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
import net.sf.xapp.marshalling.api.XMLWriter;
import net.sf.xapp.marshalling.namevaluepair.ComparableNameValuePair;
import net.sf.xapp.marshalling.namevaluepair.PropertyValuePair;
import net.sf.xapp.marshalling.namevaluepair.SimpleNameValuePair;
import net.sf.xapp.marshalling.stringserializers.EnumListSerializer;
import net.sf.xapp.marshalling.stringserializers.IntegerListSerializer;
import net.sf.xapp.marshalling.stringserializers.LongListSerializer;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objectmodelling.difftracking.ChangeModel;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeHistory;
import net.sf.xapp.utils.XappException;
import net.sf.xapp.utils.StringUtils;

import java.io.*;
import java.util.*;

public class Marshaller<T>
{
    private Class m_class;
    private ClassModel<T> m_classModel;
    private ClassDatabase m_classDatabase;
    private List<Property> m_properties;
    private HashMap<Class, Marshaller> m_marshallerMap = new HashMap<Class, Marshaller>();
    private boolean m_formatted;
    private boolean m_marshalFalseBooleanValues;
    public String m_encoding = "UTF-8";
    private boolean m_root;
    private boolean m_useCustomOrdering;

    public Marshaller(Class clazz, ClassDatabase classDatabase, boolean formatted)
    {
        this(clazz, classDatabase, formatted, true); //externally created marshallers are always root
    }

    private Marshaller(Class clazz, ClassDatabase classDatabase, boolean formatted, boolean root)
    {
        m_root = root;
        m_class = clazz;
        m_classDatabase = classDatabase;
        m_classModel = classDatabase.getClassModel(clazz);
        m_properties = m_classModel.getAllProperties();
        Collections.sort(m_properties);
        m_formatted = formatted;
        m_marshalFalseBooleanValues = Boolean.getBoolean("marshalFalseBooleanValues");
    }

    public void setUseCustomOrdering(boolean useCustomOrdering)
    {
        m_useCustomOrdering = useCustomOrdering;
    }

    public void setMarshalFalseBooleanValues(boolean marshalFalseBooleanValues)
    {
        m_marshalFalseBooleanValues = marshalFalseBooleanValues;
    }

    public Marshaller(Class clazz)
    {
        this(clazz, new ClassModelManager(clazz), true);
    }

    public void marshal(String targetFileName, T obj)
    {
        marshal(new File(targetFileName), obj);
    }

    public void marshal(File targetFile, T obj)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(targetFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, m_encoding);
            marshal(osw, obj);
            osw.flush();
            osw.close();
        }
        catch (IOException e)
        {
            throw new XappException(e);
        }
    }

    public String toXMLString(T object)
    {
        if(object==null)
        {
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        marshal(stringWriter, object);
        return stringWriter.toString();
    }

    public void marshal(Writer out, T object, String rootNodeName)
    {
        XMLWriter xmlWriter = new DefaultXMLWriter(out, m_formatted);
        marshalInternal(xmlWriter, object, rootNodeName, false);
    }

    public void marshalList(Writer out, List list, String rootNodeName)
    {
        XMLWriter xmlWriter = new DefaultXMLWriter(out, m_formatted);
        //TODO!
    }

    private void marshalInternal(XMLWriter xmlWriter, final T object, String rootNodeName, boolean includeTypeAttribute)
    {
        Class oClazz = object.getClass();
        if (!oClazz.equals(m_class))
        {
            System.out.println("WARNING!: Wrong type for this marshaller. Object's class is " + object.getClass() + ", required class is " + m_class);
        }

        if (rootNodeName == null)
        {
            rootNodeName = m_classModel.getXMLMapping();
        }


        try
        {

            List<ComparableNameValuePair> writeAsAttr = new ArrayList<ComparableNameValuePair>();
            List<PropertyValuePair> writeAsElements = new ArrayList<PropertyValuePair>();
            if (includeTypeAttribute)
            {
                writeAsAttr.add(new SimpleNameValuePair(Unmarshaller.TYPE_ATTR_TAG, object.getClass().getSimpleName()));
            }
            for (Property property : m_properties)
            {
                if (property.isTransient()) continue;

                Object value = property.get(object);

                if (value != null)
                {
                    if (property.isReference())
                    {
                        //ok we just want to marshal the id of the object
                        //the id is the name
                        ClassModel classModel = m_classDatabase.getClassModel(property.getPropertyClass());
                        writeAsAttr.add(new PropertyValuePair(property, classModel.getPrimaryKey(value)));
                    }
                    else if (property.isPrimitiveBoolean() && !m_marshalFalseBooleanValues && !(Boolean) value)
                    {
                        //skip this one!
                    }
                    //defaults to marshal as attribute for strings, enums and primitives
                    else if (!property.isFormattedText() &&
                             !isMultiline(value) &&
                            (property.isMarshalAsAttribute() ||
                                    (property.isStringPrimitiveOrEnum() && !property.isMarshalAsElement())))
                    {
                        writeAsAttr.add(new PropertyValuePair(property, value));
                    }
                    else if (value instanceof StringSerializable)
                    {
                        StringSerializable ss = (StringSerializable) value;
                        String strVal = ss.writeString();
                        writeAsAttr.add(new PropertyValuePair(property, strVal));
                    }
                    else if (property instanceof ListProperty) //skip empty lists
                    {
                        if (((ListProperty) property).getContainedType() == Integer.class)
                        {
                            writeAsAttr.add(new PropertyValuePair(property, IntegerListSerializer.doWrite((List<Integer>) value)));
                        }
                        else if (((ListProperty) property).getContainedType() == String.class)
                        {
                            writeAsAttr.add(new PropertyValuePair(property, StringUtils.convertToString((Collection<String>) value)));
                        }
                        else if (((ListProperty) property).getContainedType() == Long.class)
                        {
                            writeAsAttr.add(new PropertyValuePair(property, LongListSerializer.doWrite((List<Long>) value)));
                        }
                        else if (((ListProperty) property).getContainedTypeClassModel().isEnum())
                        {
                            writeAsAttr.add(new PropertyValuePair(property, EnumListSerializer.doWrite((List<Enum>) value)));
                        }
                        else if (value != null && !((Collection) value).isEmpty())
                        {
                            writeAsElements.add(new PropertyValuePair(property, value));
                        }
                    }
                    else
                    {
                        StringSerializer stringSerializer = m_classDatabase.getStringSerializer(property.getPropertyClass());
                        if (stringSerializer != null)
                        {
                            String s = stringSerializer.write(value);
                            writeAsAttr.add(new PropertyValuePair(property, s));
                        }
                        else if (value != null && !value.equals(""))
                        {
                            writeAsElements.add(new PropertyValuePair(property, value));
                        }
                    }
                }
            }
            //add changemodel if changes are tracked
            KeyChangeHistory changeHistory = m_classDatabase.getMarshallerContext().getKeyChangeHistory();
            boolean changeModelExists = m_root && !changeHistory.isEmpty() && m_classDatabase.getRootClassModel().equals(m_classModel);
            boolean elementsExist = !writeAsElements.isEmpty();
            xmlWriter.writeOpeningTag(rootNodeName, writeAsAttr, elementsExist);
            if (elementsExist || changeModelExists)
            {
                // Make sure the list is sorted in order to get a deterministic result with different Java versions and OS
                if (!m_useCustomOrdering)
                {
                    Collections.sort(writeAsElements);
                }

                for (PropertyValuePair propertyValuePair : writeAsElements)
                {
                    writeNode(propertyValuePair, xmlWriter, object);
                }

                if (changeModelExists)
                {
                    getMarshaller(ChangeModel.class).marshalInternal(xmlWriter, changeHistory.createChangeModel(), null, false);
                }
                xmlWriter.writeClosingTag(rootNodeName);
            }
            xmlWriter.flush();
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    protected static boolean isMultiline(Object value)
    {
        return value instanceof String && ((String)value).indexOf('\n')!=-1;
    }

    public void marshal(Writer out, T object)
    {
        try
        {
            marshal(out, object, null);
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    private void writeNode(PropertyValuePair propertyValuePair, XMLWriter out, Object parentObject) throws IOException
    {
        Object value = propertyValuePair.getValue();
        String tagName = propertyValuePair.getName();
        Property property = propertyValuePair.getProperty();
        assert value != null;
        if (isPrimitive(value) || value.getClass().equals(String.class) || value.getClass().isEnum())
        {
            out.writeSimpleTag(tagName, String.valueOf(value), property);
        }
        else if (property instanceof ContainerProperty)
        {
            ContainerProperty containerProperty = (ContainerProperty) property;

            Collection col = containerProperty.getCollection(parentObject);
            boolean elementsExist = !col.isEmpty();
            out.writeOpeningTag(tagName, null, elementsExist);

            if (elementsExist)
            {
                for (Object listItem : col)
                {
                    Class aClass = listItem.getClass();
                    if (aClass.isEnum())
                    {
                        out.writeSimpleTag(aClass.getSimpleName(), String.valueOf(listItem), null);
                        continue;
                    }
                    else if (containerProperty.containsReferences())
                    {
                        List<ComparableNameValuePair> anAttr = new ArrayList<ComparableNameValuePair>();
                        ClassModel classModel = m_classDatabase.getClassModel(aClass);
                        final String name = classModel.getPrimaryKey(listItem).toString();
                        anAttr.add(new SimpleNameValuePair("ref", name));
                        out.writeOpeningTag(aClass.getSimpleName(), anAttr, false);
                    }
                    else if (getResourceURL(listItem) != null)
                    {
                        //skip this object if it is represented as a resource
                        String resource = getResourceURL(listItem);
                        List<ComparableNameValuePair> attrTags = new ArrayList<ComparableNameValuePair>();
                        attrTags.add(new SimpleNameValuePair(Unmarshaller.DJW_INCLUDE_TAG, resource));
                        out.writeOpeningTag(listItem.getClass().getSimpleName(), attrTags, false);
                    }
                    else
                    {
                        getMarshaller(aClass).marshalInternal(out, listItem, null, false);
                    }
                }
                out.writeClosingTag(tagName);
            }
        }
        else if (getResourceURL(parentObject, property) != null)
        {
            //skip this object if it is represented as a resource
            String resource = getResourceURL(parentObject, property);
            List<ComparableNameValuePair> attrTags = new ArrayList<ComparableNameValuePair>();
            attrTags.add(new SimpleNameValuePair(Unmarshaller.DJW_INCLUDE_TAG, resource));
            out.writeOpeningTag(value.getClass().getSimpleName(), attrTags, false);
        }
        else
        {
            Class aClass = value.getClass();
            getMarshaller(aClass).marshalInternal(out, value, tagName, !aClass.equals(property.getPropertyClass()));
        }
    }

    private String getResourceURL(Object listItem)
    {
        return m_classDatabase.getMarshallerContext().getIncludedResourceURL(listItem);
    }

    private String getResourceURL(Object parentObject, Property property)
    {
        return m_classDatabase.getMarshallerContext().getIncludedResourceURLByReference(new PropertyObjectPair(property, parentObject));
    }

    private Marshaller getMarshaller(Class aClass)
    {
        Marshaller marsh = m_marshallerMap.get(aClass);
        if (marsh == null)
        {
            marsh = new Marshaller(aClass, m_classDatabase, m_formatted, false);
            marsh.setMarshalFalseBooleanValues(m_marshalFalseBooleanValues);
            marsh.setUseCustomOrdering(m_useCustomOrdering);
            m_marshallerMap.put(aClass, marsh);
        }
        return marsh;
    }

    public static void main(String[] args) throws Exception
    {
    }

    private boolean isPrimitive(Object obj)
    {
        Class cl = obj.getClass();
        return cl.equals(Boolean.class) ||
                cl.equals(Byte.class) ||
                cl.equals(Short.class) ||
                cl.equals(Character.class) ||
                cl.equals(Integer.class) ||
                cl.equals(Float.class) ||
                cl.equals(Double.class) ||
                cl.equals(Long.class);


    }

    public ClassDatabase getClassDatabase()
    {
        return m_classDatabase;
    }

    public static String toXML(Object obj)
    {
        return new Marshaller(obj.getClass()).toXMLString(obj);
    }

    public static <E> String toXML(E obj, InspectionType inspectionType)
    {
        return new Marshaller<E>(obj.getClass(), new ClassModelManager(obj.getClass(), inspectionType), true).toXMLString(obj);
    }

    public void reset(ClassDatabase classDatabase)
    {
        m_classDatabase = classDatabase;
        m_classModel = classDatabase.getClassModel(m_class);
        m_properties = m_classModel.getAllProperties();
        Collections.sort(m_properties);
    }
}
