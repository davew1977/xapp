/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.marshalling;

import net.sf.xapp.marshalling.api.StringSerializable;
import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objectmodelling.difftracking.ChangeModel;
import net.sf.xapp.utils.XappException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

public class Unmarshaller<T>
{
    private ClassModel<T> m_classModel;
    private HashMap<ClassModel, Unmarshaller> m_unmarshallerMap;
    public static final String TYPE_ATTR_TAG = "_type";
    public static final String DJW_INCLUDE_TAG = "djw-include";
    private boolean m_validate;
    private boolean m_verbose = Boolean.getBoolean("verbose");
    private boolean m_root;

    private Unmarshaller(ClassModel classModel, boolean root)
    {
        m_classModel = classModel;
        m_unmarshallerMap = new HashMap<ClassModel, Unmarshaller>();
        m_root = root;
    }

    public Unmarshaller(ClassModel classModel)
    {
        this(classModel, true);
    }

    public Unmarshaller(Class clazz)
    {
        this(new ClassModelManager(clazz).getClassModel(clazz), true);
    }

    public void setValidate(boolean validate)
    {
        m_validate = validate;
    }

    public ObjectMeta<T> unmarshal(String fileName)
    {

        File file = new File(fileName);
        return unmarshal(file);
    }

    public ObjectMeta<T> unmarshalURL(String url)
    {
        if(url.startsWith("classpath://"))
        {
            return unmarshal(Unmarshaller.class.getResourceAsStream(url.substring("classpath://".length())));
        }
        else
        {
            return unmarshal(url);
        }
    }

    public ObjectMeta<T> unmarshal(InputStream is)
    {
        return unmarshal(is, null);
    }

    public ObjectMeta<T> unmarshal(InputStream is, String path) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                documentBuilderFactory.setNamespaceAware(false);
                documentBuilderFactory.setXIncludeAware(true);
            } catch (Throwable e) {
                //e.printStackTrace();
            }
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

            Document doc = db.parse(is);
            if (m_verbose) System.out.println("trying to unmarshal: " + doc.getFirstChild());
            GlobalContext<T> context = new GlobalContext<T>();
            if (path != null) {
                context.m_path = path;
            }
            ObjectMeta<T> obj = unmarshal(doc.getDocumentElement(), context);
            /*//set primary keys
            List<ClassModel> cms = m_classModel.getClassDatabase().getClassModels();
            for (ClassModel cm : cms) {
                cm.mapAllByPrimaryKey();
            }
            //now resolve the references
            for (DelayedTask delayedTask : context.getDelayedTasks()) {
                delayedTask.execute(null);
            }*/
            //try and call post init
            for (Map.Entry<Object, Method> e : context.m_objectsWithPostInit.entrySet()) {
                ClassModel.tryAndInvoke(e.getKey(), e.getValue());
            }
            //tryAndInvoke
            if (m_validate) m_classModel.tryAndInvoke(obj, "validate");
            if (m_verbose) System.out.println("unmarshalled " + obj);
            if (m_root) {
                m_classModel.getClassDatabase().getMarshallerContext().setInitialized(obj);
            }
            return obj;
        } catch (Exception e) {
            throw new XappException(e);
        }
    }

    public ObjectMeta<T> unmarshal(File file)
    {
        try
        {
            if (!file.exists())
            {
                throw new XappException("file: " + file + " does not exist");
            }
            return unmarshal(new FileInputStream(file), file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : null);
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    public ObjectMeta<T> unmarshalString(String xml)
    {
        return unmarshalString(xml, Charset.defaultCharset());
    }

    public ObjectMeta<T> unmarshalString(String xml, Charset charset)
    {
        try
        {
            //using getBytes(String charsetName) instead of getBytes(Charset charset) to be 1.5 compliant
            return unmarshal(new ByteArrayInputStream(xml.getBytes(charset.name())));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new XappException(e);
        }
    }

    /**
     * The central unmarshalling algorithm. The xml DOM is stuffed into a java object with the
     * help of various annotations
     *
     * @param element xml element
     * @param context some meta data carried through the recursive invocation
     * @return the unmarshalled java object
     * @throws Exception any parse exception or java.reflect exception
     */
    private ObjectMeta<T> unmarshal(Element element, GlobalContext<T> context) throws Exception{
        return unmarshal(element, context, null);
    }
    private ObjectMeta<T> unmarshal(Element element, GlobalContext<T> context, Property parentProperty) throws Exception
    {
        ClassDatabase cdb = m_classModel.getClassDatabase();

        //if an element exists we should create an empty object instead of setting to null
        ObjectMeta<T> objectMeta = context.newInstance(parentProperty, m_classModel);

        NodeList nodeList = element.getChildNodes();
        for (int n = 0; n < nodeList.getLength(); n++)
        {
            Node node = nodeList.item(n);
            //skip non element nodes
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = node.getNodeName();

            //handle changeModel
            if (nodeName.equals("ChangeModel"))
            {
                ChangeModel changeModel = (ChangeModel) getUnmarshaller(ChangeModel.class).unmarshal((Element) node, context).getInstance();
                m_classModel.getClassDatabase().getMarshallerContext().getKeyChangeHistory().init(changeModel);
                continue;
            }

            Property property = m_classModel.getProperty(nodeName);
            if (property == null || property.isReadOnly())
            {
                if (m_verbose) System.out.println("method set" + nodeName + "() not found in " + m_classModel);
                continue;
            }

            Class propertyClass = property.getPropertyClass();
            StringSerializer ss = cdb.getStringSerializer(propertyClass);
            if (property.isStringOrPrimitive() || ss != null || StringSerializable.class.isAssignableFrom(propertyClass))
            {
                Node firstChild = node.getFirstChild();
                if (firstChild != null)
                {
                    String nodeValue = firstChild.getNodeValue();
                    setProperty(property, objectMeta, nodeValue, context);
                }
            }
            else if (propertyClass.isEnum())
            {
                String nodeValue = node.getFirstChild().getNodeValue();
                Enum enumValue = Enum.valueOf(propertyClass, nodeValue);
                objectMeta.set(property, enumValue);
            }
            else if (property instanceof ContainerProperty)
            {
                unmarshalList(node, property, context, objectMeta);
            }
            else
            {
                unmarshalComplexType(property, context, node, objectMeta);
            }
        }

        //now unmarshal attributes (will overwrite nested elements)
        unmarshalAttributes(element, objectMeta, context);

        if (m_classModel.hasPostInitMethod())
        {
            context.m_objectsWithPostInit.put(objectMeta.getInstance(), m_classModel.getPostInitMethod());
        }

        context.pop();

        if (m_verbose) System.out.println("unmarshalled " + objectMeta);
        return objectMeta;
    }

    private void unmarshalAttributes(Element element, ObjectMeta objMeta, GlobalContext context)
            throws IllegalAccessException, InstantiationException
    {
        NamedNodeMap attributes = element.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++)
        {
            Node attrNode = attributes.item(j);
            if (attrNode.getNodeName().equals(TYPE_ATTR_TAG)) continue;
            Property property = m_classModel.getProperty(attrNode.getNodeName());
            if (property == null || property.isReadOnly())
            {
                if (m_verbose)
                    System.out.println("method set" + attrNode.getNodeName() + "() not found in " + m_classModel);
                continue;
            }
            setProperty(property, objMeta, attrNode.getNodeValue(), context);
        }
    }

    private void unmarshalList(Node node, Property property, GlobalContext context, ObjectMeta parentOb) throws Exception
    {
        ClassDatabase classDatabase = m_classModel.getClassDatabase();
        ContainerProperty listProperty = (ContainerProperty) property;
        Object al = listProperty.createCollection();
        NodeList nl = node.getChildNodes();
        Class collectionClass = listProperty.getContainedType();
        ClassModel classModel = classDatabase.getClassModel(collectionClass);
        if (!listProperty.containsReferences())
        {

            for (int j = 0; j < nl.getLength(); j++)
            {
                Node itNode = nl.item(j);
                if (itNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element listElement = (Element) itNode;
                    Object nextObject = null;
                    Node includeResource = listElement.getAttributes().getNamedItem(DJW_INCLUDE_TAG);
                    if (classModel.isEnum())
                    {
                        nextObject = Enum.valueOf(collectionClass, listElement.getFirstChild().getNodeValue());
                    }
                    else if (classModel.isAbstract())
                    {
                        //need to find implementation type
                        ClassModel validImplementation = classModel.getValidImplementation(listElement.getNodeName());
                        Unmarshaller unmarshaller = getUnmarshaller(validImplementation);
                        nextObject = unmarshaller.unmarshal(listElement, context, property).getInstance();
                    }
                    else if (includeResource != null)
                    {
                        nextObject = getIncludedResource(includeResource, classModel, context);
                        m_classModel.getClassDatabase().getMarshallerContext().mapIncludedResourceURL(nextObject, includeResource.getNodeValue());
                    }
                    else
                    {
                        Unmarshaller unmarshaller = getUnmarshaller(classModel.getClassDatabase().getClassModel(collectionClass));
                        nextObject = unmarshaller.unmarshal(listElement, context, property).getInstance();
                    }
                    listProperty.addToMapOrCollection(al, -1, nextObject);
                }
            }
            parentOb.set(property, al);
        }
        else
        {
            SetReferenceListTask setReferenceListTask = new SetReferenceListTask(listProperty, parentOb);
            for (int j = 0; j < nl.getLength(); j++)
            {
                Node itNode = nl.item(j);
                if (itNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element listElement = (Element) itNode;
                    String refAttr = listElement.getAttribute("ref");
                    setReferenceListTask.m_references.add(refAttr);
                }
            }
            context.add(setReferenceListTask);
        }
    }

    private void unmarshalComplexType(Property property, GlobalContext context, Node node, ObjectMeta parentOb) throws Exception
    {
        ClassDatabase classDatabase = m_classModel.getClassDatabase();
        Class propertyClass = property.getPropertyClass();

        ClassModel classModel = classDatabase.getClassModel(propertyClass);
        Object newObj = null;
        Node includeResource = node.getAttributes().getNamedItem(DJW_INCLUDE_TAG);
        if (classModel.isAbstract())
        {
            Node className = node.getAttributes().getNamedItem(TYPE_ATTR_TAG);
            //if class name is null then try at least to instantiate "abstract class"
            ClassModel validImplementation = className != null ?
                    classModel.getValidImplementation(className.getNodeValue()) : classModel;
            Unmarshaller unmarshaller = getUnmarshaller(validImplementation);
            newObj = unmarshaller.unmarshal((Element) node, context, property).getInstance();
        }
        else if (includeResource != null)
        {
            newObj = getIncludedResource(includeResource, classModel, context);
            PropertyObjectPair reference = new PropertyObjectPair(property, parentOb);
            m_classModel.getClassDatabase().getMarshallerContext().mapIncludedResourceURLByReference(reference, includeResource.getNodeValue());
        }
        else
        {
            newObj = getUnmarshaller(classModel).unmarshal((Element) node, context, property).getInstance();
        }
        parentOb.set(property, newObj);

    }

    private Object getIncludedResource(Node includeResource, ClassModel classModel, GlobalContext context)
    {
        Object newObj = null;
        String resourceURL = includeResource.getNodeValue();
        Unmarshaller unmarshaller = getUnmarshaller(classModel);
        File file = new File(resourceURL);
        if (file.exists())
        {
            newObj = unmarshaller.unmarshal(resourceURL);
        }
        else
        {
            file = new File(context.m_path, resourceURL);
            if (file.exists())
            {
                newObj = unmarshaller.unmarshal(file.getAbsolutePath());
            }
            else
            {
                //try to load from classpath
                InputStream asStream = getClass().getResourceAsStream(resourceURL);
                if (asStream != null)
                {
                    newObj = unmarshaller.unmarshal(asStream);
                }
                else
                {
                    System.out.println("WARNING: " + resourceURL + " does not exist");
                    System.out.println("PATH: " + context.m_path);
                }
            }
        }
        return newObj;
    }

    private Unmarshaller getUnmarshaller(Class aClass)
    {
        return getUnmarshaller(m_classModel.getClassDatabase().getClassModel(aClass));
    }

    private Unmarshaller getUnmarshaller(ClassModel classModel)
    {
        Unmarshaller unmarshaller = m_unmarshallerMap.get(classModel);
        if (unmarshaller == null)
        {
            unmarshaller = new Unmarshaller(classModel, false);
            unmarshaller.m_verbose = m_verbose;
            m_unmarshallerMap.put(classModel, unmarshaller);
        }
        return unmarshaller;
    }

    public void setProperty(Property property, ObjectMeta target, String nodeValue, GlobalContext context) throws IllegalAccessException, InstantiationException
    {
        if (property.isReference())
        {
            context.add(new SetReferenceTask(property, target, nodeValue));
        }
        else
        {
            property.setSpecial(target, nodeValue);
        }
    }

    public ClassDatabase getClassDatabase()
    {
        return m_classModel.getClassDatabase();
    }

    public ClassModel getClassModel()
    {
        return m_classModel;
    }

    /**
     * this is necessary if you want to reuse an unmarshaller after reseting the class database, because
     * the cached reference to the classmodel here is stale
     * @param cdb
     */
    public void reset(ClassDatabase cdb)
    {
        m_classModel = cdb.getClassModel(m_classModel.getContainedClass());
    }

    private static class GlobalContext<T>
    {
        public Map<Object, Method> m_objectsWithPostInit = new LinkedHashMap<Object, Method>();
        public String m_path;
        Stack<LocalContext> contextStack = new Stack<LocalContext>();

        private GlobalContext() {
        }

        public <E> ObjectMeta<E> newInstance(Property property, ClassModel<E> classModel) {

            LocalContext<E> localContext = new LocalContext<E>(currentContext(), classModel);
            contextStack.push(localContext);
            return localContext.newInstance(property);
        }

        private LocalContext currentContext() {
            return !contextStack.isEmpty() ? contextStack.peek() : null;
        }

        public void pop() {
            LocalContext localContext = contextStack.pop();
            localContext.postInit();
        }

        public void add(SetRefTask setReferenceTask) {
            LocalContext context = currentContext().getNamespace(setReferenceTask.getPropertyType());
            context.addDelayedTask(setReferenceTask);
        }
    }

    private static class LocalContext<E> {
        LocalContext parentContext;
        ObjectMeta<E> objectMeta;
        ClassModel<E> classModel;
        List<SetRefTask> setRefTasks = new ArrayList<SetRefTask>();
        //List<Object> childObjectsToMap = new ArrayList<Object>();

        public LocalContext(LocalContext parentContext, ClassModel<E> classModel) {
            this.parentContext = parentContext;
            this.classModel = classModel;
        }

        void addDelayedTask(SetRefTask setRefTask) {
            setRefTasks.add(setRefTask);
        }

        public List<SetRefTask> getSetRefTasks() {
            return setRefTasks;
        }

        /*public void addChildObjectToMap(Object child) {
            childObjectsToMap.add(child);
        }*/

        /*public void mapAllByKey() {
            ClassDatabase cdb = classModel.getClassDatabase();
            for (Object o : childObjectsToMap) {
                Class<?> aClass = o.getClass();
                ClassModel cm = cdb.getClassModel(aClass);
                String key = cm.getKey(o);
                objectMeta.add(aClass, key, o);
            }
            childObjectsToMap = null;
        }*/


        public void setRefs() {
            assert setRefTasks.isEmpty() || objectMeta!=null;
            for (SetRefTask setRefTask : getSetRefTasks()) {
                setRefTask.execute(objectMeta);
            }
        }

        public boolean isNamespaceFor(Class propertyClass) {
            return objectMeta != null && objectMeta.isNamespaceFor(propertyClass);
        }

        public LocalContext getNamespace(Class aClass) {
            return isNamespaceFor(aClass) ? this : parentContext.getNamespace(aClass);
        }


        public  ObjectMeta<E> newInstance(Property property) {
            objectMeta = classModel.newInstance(!isRoot() ? new ObjectLocation(parentObjMeta(), property) : null);

            /*if (classModel.hasKey()) {
                LocalContext namespace = getNamespace(classModel.getContainedClass());
                namespace.addChildObjectToMap(objectMeta.getInstance());
                //todo maybe store a reference to its namespace
            }*/
            return objectMeta;
        }

        private ObjectMeta parentObjMeta() {
            return !isRoot() ? parentContext.objectMeta : null;
        }

        public void postInit() {
            //mapAllByKey();
            setRefs();
        }

        public boolean isRoot() {
            return parentContext == null;
        }
    }

    public static abstract class SetRefTask
    {
        Property property;
        ObjectMeta target;

        protected SetRefTask(Property property, ObjectMeta m_target) {
            this.property = property;
            this.target = m_target;
        }

        abstract void execute(ObjectMeta objectMeta);

        abstract Class getPropertyType();
    }

    private static class SetReferenceTask extends SetRefTask
    {
        String m_reference;

        public SetReferenceTask(Property property, ObjectMeta target, String reference)
        {
            super(property, target);
            m_reference = reference;
        }

        public void execute(ObjectMeta objectMeta)
        {
            ObjectMeta objMeta = objectMeta.getObjMeta(property.getPropertyClass(), m_reference);
            objMeta.createAndSetReference(new ObjectLocation(target, property));
        }

        @Override
        Class getPropertyType() {
            return property.getPropertyClass();
        }
    }

    private class SetReferenceListTask extends SetRefTask
    {
        ContainerProperty m_listProperty;
        List<String> m_references;

        public SetReferenceListTask(ContainerProperty listProperty, ObjectMeta target)
        {
            super(listProperty, target);
            m_listProperty = listProperty;
            m_references = new ArrayList<String>();
        }

        public void execute(ObjectMeta objectMeta)
        {
            Object mapOrCollection = m_listProperty.createCollection();
            for (String s : m_references)
            {
                Class propertyClass = m_listProperty.getContainedType();
                ObjectMeta ref = objectMeta.getObjMeta(propertyClass, s);
                ref.createAndSetReference(new ObjectLocation(target, m_listProperty, -1));
            }
            target.set(m_listProperty, mapOrCollection);
        }

        @Override
        Class getPropertyType() {
            return m_listProperty.getContainedType();
        }
    }

    public static ClassDatabase loadClassDatabase(Class aClass, InputStream is)
    {
        Unmarshaller u = new Unmarshaller(aClass);
        u.unmarshal(is);
        return u.getClassDatabase();
    }
    public static <E> E load(ClassDatabase<?> cdb, Class<E> aClass, InputStream is)
    {
        return cdb.createUnmarshaller(aClass).unmarshal(is).getInstance();
    }

    /**
     * if url startwith classpath:// then the following string will be taken to be a classpath
     * otherwise it will consider it to be a file path
     * @param aClass
     * @param url
     * @return
     */
    public static <E> E  load(ClassDatabase<?> cdb, Class<E> aClass, String url)
    {
        Unmarshaller<E> u = cdb.createUnmarshaller(aClass);
        return u.unmarshalURL(url).getInstance();
    }

    public static <E> E load(Class<E> aClass, String url)
    {
        return load(aClass, url, InspectionType.METHOD);
    }

    public static <E> E load(Class<E> aClass, String url, InspectionType inspectionType)
    {
        return load(new ClassModelManager<E>(aClass, inspectionType), aClass,  url);
    }

    public static <E> E  load(ClassDatabase<E> cdb, String url)
    {
        return load(cdb, cdb.getRootClassModel().getContainedClass(), url);
    }
}
