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
import net.sf.xapp.utils.XappException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class Unmarshaller<T> {
    private ClassModel<T> classModel;
    private HashMap<ClassModel, Unmarshaller> m_unmarshallerMap;
    public static final String ATTR_TYPE = "_type";
    public static final String ATTR_ID = "_id";
    public static final String ATTR_REV = "_rev";
    public static final String DJW_INCLUDE_TAG = "djw-include";
    private boolean m_validate;
    private boolean m_verbose = Boolean.getBoolean("verbose");
    private boolean m_root;

    private Unmarshaller(ClassModel classModel, boolean root) {
        this.classModel = classModel;
        m_unmarshallerMap = new HashMap<ClassModel, Unmarshaller>();
        m_root = root;
    }

    public Unmarshaller(ClassModel classModel) {
        this(classModel, true);
    }

    public Unmarshaller(Class clazz) {
        this(new ClassModelManager(clazz).getClassModel(clazz), true);
    }

    public void setValidate(boolean validate) {
        m_validate = validate;
    }

    public ObjectMeta<T> unmarshal(String fileName) {

        File file = new File(fileName);
        return unmarshal(file);
    }

    public ObjectMeta<T> unmarshalURL(String url) {
        if (url.startsWith("classpath://")) {
            return unmarshal(Unmarshaller.class.getResourceAsStream(url.substring("classpath://".length())));
        } else {
            return unmarshal(url);
        }
    }

    public ObjectMeta<T> unmarshal(InputStream is) {
        return unmarshal(is, null);
    }

    public ObjectMeta<T> unmarshal(InputStream is, ObjectLocation parent) {
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
            ObjectMeta<T> obj = unmarshal(doc.getDocumentElement(), parent);
            //if parent is not null, we may have pending references to set some higher up the hierarchy
            if (parent != null) {
                parent.flushPendingRefs();
            }
            //tryAndInvoke
            if (m_validate) {
                ClassModel.tryAndInvoke(obj, "validate");
            }
            if (m_verbose) {
                System.out.println("unmarshalled " + obj);
            }
            if (m_root) {
                classModel.getClassDatabase().getMarshallerContext().setInitialized(obj);
            }
            return obj;
        } catch (Exception e) {
            throw new XappException(e);
        }
    }

    public ObjectMeta<T> unmarshal(File file) {
        try {
            if (!file.exists()) {
                throw new XappException("file: " + file + " does not exist");
            }
            return unmarshal(new FileInputStream(file));
        } catch (Exception e) {
            throw new XappException(e);
        }
    }

    public ObjectMeta<T> unmarshalString(String xml) {
        return unmarshalString(xml, Charset.defaultCharset());
    }

    public ObjectMeta<T> unmarshalString(String xml, Charset charset) {
        return unmarshalString(xml, charset, null);
    }

    public ObjectMeta<T> unmarshalString(String xml, Charset charset, ObjectLocation parent) {
        try {
            //using getBytes(String charsetName) instead of getBytes(Charset charset) to be 1.5 compliant
            return unmarshal(new ByteArrayInputStream(xml.getBytes(charset.name())), parent);
        } catch (UnsupportedEncodingException e) {
            throw new XappException(e);
        }
    }

    /**
     * The central unmarshalling algorithm. The xml DOM is stuffed into a java object with the
     * help of various annotations
     */
    private ObjectMeta<T> unmarshal(Element element, ObjectLocation parent) throws Exception {
        ClassDatabase cdb = classModel.getClassDatabase();

        //if an element exists we should create an empty object instead of setting to null
        ObjectMeta<T> objectMeta = classModel.newInstance(parent, true);

        if (!getClassDatabase().isMaster()) {
            Attr attributeNode = element.getAttributeNode(ATTR_ID);
            if(attributeNode != null) {
                objectMeta.setId(Long.parseLong(attributeNode.getNodeValue()));
            }
        }

        Attr attributeNode = element.getAttributeNode(ATTR_REV);
        if(attributeNode != null) {
            objectMeta.setRev(Long.parseLong(attributeNode.getNodeValue()));
        }

        NodeList nodeList = element.getChildNodes();
        for (int n = 0; n < nodeList.getLength(); n++) {
            Node node = nodeList.item(n);
            //skip non element nodes
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = node.getNodeName();

            Property property = classModel.getProperty(nodeName);
            if (property == null && classModel.getContainerProperty() != null) {
                unmarshalListItem(new ObjectLocation(objectMeta, classModel.getContainerProperty()), (Element) node);
                continue;
            } else if (property == null || property.isReadOnly()) {
                if (m_verbose) System.out.println("method set" + nodeName + "() not found in " + classModel);
                continue;
            }
            ObjectLocation objLocation = new ObjectLocation(objectMeta, property);
            Class propertyClass = property.getPropertyClass();
            StringSerializer ss = cdb.getStringSerializer(propertyClass);
            if (property.isStringOrPrimitive() || ss != null || StringSerializable.class.isAssignableFrom(propertyClass)) {
                Node firstChild = node.getFirstChild();
                if (firstChild != null) {
                    String nodeValue = firstChild.getNodeValue();
                    setProperty(objLocation, nodeValue);
                }
            } else if (propertyClass.isEnum()) {
                String nodeValue = node.getFirstChild().getNodeValue();
                Enum enumValue = Enum.valueOf(propertyClass, nodeValue);
                objectMeta.set(property, enumValue);
            } else if (property instanceof ContainerProperty) {
                unmarshalList(node, objLocation);
            } else {
                unmarshalComplexType(node, objLocation);
            }
        }

        //now unmarshal attributes (will overwrite nested elements)
        unmarshalAttributes(element, objectMeta);

        objectMeta.postInit();

        if (m_verbose) {
            System.out.println("unmarshalled " + objectMeta);
        }
        return objectMeta;
    }

    private void unmarshalAttributes(Element element, ObjectMeta objMeta)
            throws IllegalAccessException, InstantiationException {
        NamedNodeMap attributes = element.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            Node attrNode = attributes.item(j);
            if (attrNode.getNodeName().equals(ATTR_TYPE)) continue;
            Property property = classModel.getProperty(attrNode.getNodeName());
            if (property == null || property.isReadOnly()) {
                if (m_verbose)
                    System.out.println("method set" + attrNode.getNodeName() + "() not found in " + classModel);
                continue;
            }
            setProperty(new ObjectLocation(objMeta, property), attrNode.getNodeValue());
        }
    }

    private void unmarshalList(Node node, ObjectLocation objectLocation) throws Exception {
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node itNode = childNodes.item(j);
            if (itNode.getNodeType() == Node.ELEMENT_NODE) {
                if (!objectLocation.containsReferences()) {
                    unmarshalListItem(objectLocation, (Element) itNode);
                } else {
                    objectLocation.addPendingRef(((Element) itNode).getAttribute("ref"));
                }
            }
        }
    }

    private void unmarshalListItem(ObjectLocation objectLocation, Element itNode) throws Exception {
        ClassDatabase classDatabase = classModel.getClassDatabase();

        Class collectionClass = objectLocation.getPropertyClass();
        ClassModel classModel = classDatabase.getClassModel(collectionClass);
        if (classModel.isEnum()) {
            Enum nextObject = Enum.valueOf(collectionClass, itNode.getFirstChild().getNodeValue());
            objectLocation.getCollection().add(nextObject);
        } else if (classModel.isAbstract()) {
            //need to find implementation type
            ClassModel validImplementation = classModel.getValidImplementation(itNode.getNodeName());
            Unmarshaller unmarshaller = getUnmarshaller(validImplementation);
            unmarshaller.unmarshal(itNode, objectLocation).getInstance();
        } else {
            Unmarshaller unmarshaller = getUnmarshaller(classModel.getClassDatabase().getClassModel(collectionClass));
            unmarshaller.unmarshal(itNode, objectLocation).getInstance();
        }
        //could be a problem if we reimplement includeresource listProperty.addToMapOrCollection(al, -1, nextObject);
    }

    private void unmarshalComplexType(Node node, ObjectLocation objectLocation) throws Exception {
        ClassDatabase classDatabase = classModel.getClassDatabase();
        ClassModel classModel = classDatabase.getClassModel(objectLocation.getPropertyClass());
        if (classModel.isAbstract()) {
            Node className = node.getAttributes().getNamedItem(ATTR_TYPE);
            //if class name is null then try at least to instantiate "abstract class"
            classModel = className != null ?
                    classModel.getValidImplementation(className.getNodeValue()) : classModel;
        }
        getUnmarshaller(classModel).unmarshal((Element) node, objectLocation).getInstance();
    }

    private Object getIncludedResource(Node includeResource, ClassModel classModel) {
        throw new UnsupportedOperationException();
    }

    private Unmarshaller getUnmarshaller(Class aClass) {
        return getUnmarshaller(classModel.getClassDatabase().getClassModel(aClass));
    }

    private Unmarshaller getUnmarshaller(ClassModel classModel) {
        Unmarshaller unmarshaller = m_unmarshallerMap.get(classModel);
        if (unmarshaller == null) {
            unmarshaller = new Unmarshaller(classModel, false);
            unmarshaller.m_verbose = m_verbose;
            m_unmarshallerMap.put(classModel, unmarshaller);
        }
        return unmarshaller;
    }

    public void setProperty(ObjectLocation objectLocation, String nodeValue) throws IllegalAccessException, InstantiationException {
        if (objectLocation.isReference()) {
            objectLocation.addPendingRef(nodeValue);
        } else {
            objectLocation.set(nodeValue);
        }
    }

    public ClassDatabase getClassDatabase() {
        return classModel.getClassDatabase();
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    /**
     * this is necessary if you want to reuse an unmarshaller after reseting the class database, because
     * the cached reference to the classmodel here is stale
     *
     * @param cdb
     */
    public void reset(ClassDatabase cdb) {
        classModel = cdb.getClassModel(classModel.getContainedClass());
    }

    public static ClassDatabase loadClassDatabase(Class aClass, InputStream is) {
        Unmarshaller u = new Unmarshaller(aClass);
        u.unmarshal(is);
        return u.getClassDatabase();
    }

    public static <E> E load(ClassDatabase<?> cdb, Class<E> aClass, InputStream is) {
        return cdb.createUnmarshaller(aClass).unmarshal(is).getInstance();
    }

    /**
     * if url startwith classpath:// then the following string will be taken to be a classpath
     * otherwise it will consider it to be a file path
     *
     * @param aClass
     * @param url
     * @return
     */
    public static <E> E load(ClassDatabase<?> cdb, Class<E> aClass, String url) {
        Unmarshaller<E> u = cdb.createUnmarshaller(aClass);
        return u.unmarshalURL(url).getInstance();
    }

    public static <E> E load(Class<E> aClass, String url) {
        return load(aClass, url, InspectionType.METHOD);
    }

    public static <E> E load(Class<E> aClass, String url, InspectionType inspectionType) {
        return load(new ClassModelManager<E>(aClass, inspectionType), aClass, url);
    }

    public static <E> E load(ClassDatabase<E> cdb, String url) {
        return load(cdb, cdb.getRootClassModel().getContainedClass(), url);
    }
}
