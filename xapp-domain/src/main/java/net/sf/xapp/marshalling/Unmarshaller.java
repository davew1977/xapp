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
import net.sf.xapp.tree.Tree;
import net.sf.xapp.tree.TreeNode;
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

    public T unmarshal(String fileName)
    {

        File file = new File(fileName);
        return unmarshal(file);
    }

    public T unmarshalURL(String url)
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

    public T unmarshal(InputStream is)
    {
        return unmarshal(is, null);
    }

    public T unmarshal(InputStream is, String path)
    {
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try
            {
                documentBuilderFactory.setNamespaceAware(false);
                documentBuilderFactory.setXIncludeAware(true);
            }
            catch (Throwable e)
            {
                //e.printStackTrace();
            }
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

            Document doc = db.parse(is);
            if (m_verbose) System.out.println("trying to unmarshal: " + doc.getFirstChild());
            Context context = new Context();
            if(path != null)
            {
                context.m_path = path;
            }
            T obj = unmarshal(doc.getDocumentElement(), context, null);
            //set primary keys
            List<ClassModel> cms = m_classModel.getClassDatabase().getClassModels();
            for (ClassModel cm : cms)
            {
                cm.mapAllByPrimaryKey();
            }
            //now resolve the references
            for (DelayedTask delayedTask : context.m_delayedTasks)
            {
                delayedTask.execute();
            }
            //try and call post init
            for (Map.Entry<Object, Method> e : context.m_objectsWithPostInit.entrySet())
            {
                ClassModel.tryAndInvoke(e.getKey(), e.getValue());
            }
            //tryAndInvoke
            if (m_validate) m_classModel.tryAndInvoke(obj, "validate");
            if (m_verbose) System.out.println("unmarshalled " + obj);
            if (m_root)
            {
                m_classModel.getClassDatabase().getMarshallerContext().setInitialized(obj);
            }
            return obj;
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    public T unmarshal(File file)
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

    public T unmarshalString(String xml)
    {
        return unmarshalString(xml, Charset.defaultCharset());
    }

    public T unmarshalString(String xml, Charset charset)
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
    private T unmarshal(Element element, Context context, Object parent) throws Exception
    {
        ClassDatabase classDatabase = m_classModel.getClassDatabase();

        //if an element exists we should create an empty object instead of setting to null
        T obj = m_classModel.newInstance(parent);

        NodeList nodeList = element.getChildNodes();
        for (int n = 0; n < nodeList.getLength(); n++)
        {
            Node node = nodeList.item(n);
            //skip non element nodes
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            String nodeName = node.getNodeName();

            //handle changeModel
            if (nodeName.equals("ChangeModel"))
            {
                ChangeModel changeModel = (ChangeModel) getUnmarshaller(ChangeModel.class).unmarshal((Element) node, context, null);
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
            StringSerializer ss = classDatabase.getStringSerializer(propertyClass);
            if (property.isStringOrPrimitive() || ss != null || StringSerializable.class.isAssignableFrom(propertyClass))
            {
                Node firstChild = node.getFirstChild();
                if (firstChild != null)
                {
                    String nodeValue = firstChild.getNodeValue();
                    setProperty(property, obj, nodeValue, context);
                }
            }
            else if (propertyClass.isEnum())
            {
                String nodeValue = node.getFirstChild().getNodeValue();
                Enum enumValue = Enum.valueOf(propertyClass, nodeValue);
                property.set(obj, enumValue);
            }
            else if (property instanceof ListProperty)
            {
                unmarshalList(node, property, context, obj);
            }
            else
            {
                unmarshalComplexType(property, context, node, obj);
            }
        }

        //now unmarshal attributes (will overwrite nested elements)
        unmarshalAttributes(element, obj, context);

        /*if (m_classModel.hasPrimaryKey() && !context.m_inTree) //keys are handled separately in subtrees
        {
            m_classModel.mapByPrimaryKey(obj);
        }*/
        //now inject class model object if the class fits
        m_classModel.injectClassModel(obj);
        if (m_classModel.hasPostInitMethod())
        {
            context.m_objectsWithPostInit.put(obj, m_classModel.getPostInitMethod());
        }
        if (m_verbose) System.out.println("unmarshalled " + obj);
        return obj;
    }

    private void unmarshalAttributes(Element element, Object obj, Context context)
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
            setProperty(property, obj, attrNode.getNodeValue(), context);
        }
    }

    private void unmarshalList(Node node, Property property, Context context, Object parentObj) throws Exception
    {
        ClassDatabase classDatabase = m_classModel.getClassDatabase();
        ListProperty listProperty = (ListProperty) property;
        Collection al = listProperty.createCollection();
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
                    //check for namespace
                    if (listProperty.isNewNamespace())
                    {
                        ClassModelManager classModelManager = classModel.getClassDatabase().getMarshallerContext().createChildCMM(listProperty.getSharedInNamespace());
                        classModel = classModelManager.getClassModel(classModel.getContainedClass());
                    }
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
                        nextObject = unmarshaller.unmarshal(listElement, context, parentObj);
                    }
                    else if (includeResource != null)
                    {
                        nextObject = getIncludedResource(includeResource, classModel, context);
                        m_classModel.getClassDatabase().getMarshallerContext().mapIncludedResourceURL(nextObject, includeResource.getNodeValue());
                    }
                    else
                    {
                        Unmarshaller unmarshaller = getUnmarshaller(classModel.getClassDatabase().getClassModel(collectionClass));
                        nextObject = unmarshaller.unmarshal(listElement, context, parentObj);
                    }
                    al.add(nextObject);
                }
            }
            property.set(parentObj, al);
        }
        else
        {
            SetReferenceListTask setReferenceListTask = new SetReferenceListTask(listProperty, parentObj);
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
            context.m_delayedTasks.add(setReferenceListTask);
        }
    }

    private void unmarshalComplexType(Property property, Context context, Node node, Object parentObj) throws Exception
    {
        ClassDatabase classDatabase = m_classModel.getClassDatabase();
        Class propertyClass = property.getPropertyClass();
        //if this is a tree property then add to valid implementations
        if (property.isTree())
        {
            if (context.m_inTree)
                throw new XappException("nested tree not supported, property: " + property);
            context.m_inTree = true;
            List<ClassModel> extraTypes = classDatabase.getClassModels(property.getTreeMeta().leafTypes());
            //extraTypes.add(cmm.getClassModel(property.getTreeMeta().nodeSetType()));
            classDatabase.getClassModel(TreeNode.class).addValidImplementations(extraTypes);
        }

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
            newObj = unmarshaller.unmarshal((Element) node, context, parentObj);
        }
        else if (includeResource != null)
        {
            newObj = getIncludedResource(includeResource, classModel, context);
            PropertyObjectPair reference = new PropertyObjectPair(property, parentObj);
            m_classModel.getClassDatabase().getMarshallerContext().mapIncludedResourceURLByReference(reference, includeResource.getNodeValue());
        }
        else
        {
            newObj = getUnmarshaller(classModel).unmarshal((Element) node, context, parentObj);
        }
        property.set(parentObj, newObj);

        //if property is a tree
        if (property.isTree() && newObj != null)
        {
            Tree tree = (Tree) newObj;
            tree.updateTree(property.getTreeMeta().pathSeparator()); //updates key property
            tree.setLeafTypes(property.getTreeMeta().leafTypes());
            context.m_inTree = false;
        }
    }

    private Object getIncludedResource(Node includeResource, ClassModel classModel, Context context)
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

    public void setProperty(Property property, Object target, String nodeValue, Context context) throws IllegalAccessException, InstantiationException
    {
        if (property.isReference())
        {
            context.m_delayedTasks.add(new SetReferenceTask(property, target, nodeValue));
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

    private class Context
    {
        List<DelayedTask> m_delayedTasks = new ArrayList<DelayedTask>();
        boolean m_inTree;
        public Map<Object, Method> m_objectsWithPostInit = new LinkedHashMap<Object, Method>();
        public String m_path;
    }

    private static interface DelayedTask
    {
        void execute();
    }

    private static class SetReferenceTask implements DelayedTask
    {
        Property m_property;
        Object m_target;
        String m_reference;

        public SetReferenceTask(Property property, Object target, String reference)
        {
            m_property = property;
            m_target = target;
            m_reference = reference;
        }

        public void execute()
        {
            m_property.setSpecial(m_target, m_reference);
        }
    }

    private class SetReferenceListTask implements DelayedTask
    {
        ListProperty m_listProperty;
        Object m_target;
        List<String> m_references;

        public SetReferenceListTask(ListProperty listProperty, Object target)
        {
            m_listProperty = listProperty;
            m_target = target;
            m_references = new ArrayList<String>();
        }

        public void execute()
        {
            List list = new ArrayList();
            for (String s : m_references)
            {
                Class propertyClass = m_listProperty.getContainedType();
                ClassModel classModel = m_classModel.getClassDatabase().getClassModel(propertyClass);
                Object ref = classModel.getInstance(s);
                list.add(ref);
            }
            m_listProperty.set(m_target, list);
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
        return cdb.createUnmarshaller(aClass).unmarshal(is);
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
        return u.unmarshalURL(url);
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