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
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.application.Hide;
import net.sf.xapp.annotations.application.Validate;
import net.sf.xapp.annotations.objectmodelling.*;
import net.sf.xapp.annotations.marshalling.MarshalAsAttribute;
import net.sf.xapp.annotations.marshalling.MarshalAsElement;
import net.sf.xapp.annotations.marshalling.XMLMapping;
import net.sf.xapp.marshalling.api.StringSerializable;
import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.utils.XappException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A property encapsulates meta data about a class's property. A property is an attribute of a class that is
 * accessible through a getter and optionally modifiable through a setter
 */
public class Property<T> implements Comparable
{
    protected final PropertyAccess m_propertyAccess;
    protected Class m_class;//class of return type of accessor
    private Reference m_reference;//true if this should point to another object in the datamodel
    private String m_filterOnProperty;
    private boolean key;
    private EditorWidget m_editorWidget; //override the default bound component
    protected ClassDatabase m_classDatabase;
    private boolean m_formattedText;
    private Class m_parentClass;
    private String m_query = "";
    private boolean m_editable;
    private boolean m_editableOnCreation;
    private TreeMeta m_treeMeta;
    private List<PropertyChangeListener> m_listeners;
    private boolean m_mandatory;
    private boolean m_visibilityRestricted = true; //defaults to visible in dynamic GUI

    public Property(ClassModelManager classDatabase, PropertyAccess propertyAccess, Class aClass, Reference ref,
                    String filterOnProperty, Key key,
                    EditorWidget editorWidget,
                    boolean isformattedText,
                    Class parentClass,
                    String query,
                    boolean editable,
                    TreeMeta treeMeta,
                    boolean mandatory)
    {
        m_classDatabase = classDatabase;
        m_mandatory = mandatory;
        m_propertyAccess = propertyAccess;
        m_class = aClass;
        m_reference = ref;
        m_filterOnProperty = filterOnProperty;
        this.key = key !=null;
        m_editorWidget = editorWidget;
        m_formattedText = isformattedText;
        m_parentClass = parentClass;
        m_query = query;
        m_editable = editable;
        m_editableOnCreation = editable;
        m_treeMeta = treeMeta;
        m_listeners = new ArrayList<PropertyChangeListener>();
    }

    public void addChangeListener(PropertyChangeListener listener)
    {
        m_listeners.add(listener);
    }

    public void setVisibilityRestricted(boolean visibilityRestricted)
    {
        m_visibilityRestricted = visibilityRestricted;
    }

    public void setEditable(boolean editable)
    {
        m_editable = editable;
    }

    public void setEditableOnCreation(boolean editableOnCreation)
    {
        m_editableOnCreation = editableOnCreation;
    }

    public boolean isEditableOnCreation()
    {
        return m_editableOnCreation;
    }

    public Object get(Object target)
    {
        try
        {
            return m_propertyAccess.get(target);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("WARNING: property " + this + " in " + m_parentClass.getSimpleName() + " cannot be retrieved on " + target + " of class " + target.getClass().getSimpleName());
            return null;
        }
    }

    public PropertyChange set(T target, Object newVal)
    {
        try
        {
            Object oldVal = get(target);
            if (!objEquals(oldVal, newVal))
            {
                m_propertyAccess.set(target, newVal);
                for (PropertyChangeListener listener : m_listeners)
                {
                    listener.propertyChanged(this, target, oldVal, newVal);
                }
                return new PropertyChange(this, target, oldVal, newVal);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("WARNING: property " + this + " in " + m_parentClass.getSimpleName() + " cannot be set on " + target + " of class " + target.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Set a property from a string value
     *
     * @param target
     * @param value
     */
    public void setSpecial(ObjectMeta<T> target, String value)
    {
        Object obj = convert(target, value);

        target.set(this, obj);
    }

    public Object convert(ObjectMeta objectMeta, String value)
    {
        Class propertyClass = getPropertyClass();
        StringSerializer strSerializer = getClassDatabase().getStringSerializer(propertyClass);
        Object obj = null;
        if (propertyClass.equals(String.class))
        {
            obj = value;
        }
        else if (propertyClass.equals((boolean.class)) || propertyClass.equals(Boolean.class))
        {
            obj = Boolean.valueOf(value);
        }
        else if (propertyClass.equals((int.class)) || propertyClass.equals(Integer.class))
        {
            obj = new Integer(value);
        }
        else if (propertyClass.equals((short.class)) || propertyClass.equals(Short.class))
        {
            obj = new Short(value);
        }
        else if (propertyClass.equals((byte.class)) || propertyClass.equals(Byte.class))
        {
            obj = new Byte(value);
        }
        else if (propertyClass.equals((char.class)) || propertyClass.equals(Character.class))
        {
            obj = value.charAt(0);
        }
        else if (propertyClass.equals((float.class)) || propertyClass.equals(Float.class))
        {
            obj = new Float(value);
        }
        else if (propertyClass.equals((double.class)) || propertyClass.equals(Double.class))
        {
            obj = new Double(value);
        }
        else if (propertyClass.equals((long.class)) || propertyClass.equals(Long.class))
        {
            obj = new Long(value);
        }
        else if (propertyClass.isEnum())
        {
            obj = Enum.valueOf(propertyClass, value);
        }
        else if (isReference())
        {
            obj = objectMeta.get(propertyClass, value);
        }
        else if (strSerializer != null)
        {
            obj = strSerializer.read(value);
        }
        else if (StringSerializable.class.isAssignableFrom(propertyClass))
        {
            StringSerializable o = null;
            try
            {
                o = (StringSerializable) propertyClass.newInstance();
            }
            catch (Exception e)
            {
                throw new XappException(e);
            }
            o.readString(value);
            obj = o;
        }
        else throw new XappException("property " + this + " is not StringSerializable");
        return obj;
    }

    public boolean isStringSerializable()
    {
        return StringSerializable.class.isAssignableFrom(getPropertyClass()) ||
                getClassDatabase().getStringSerializer(getPropertyClass()) != null;
    }

    public String toString(Object val)
    {
        if (val == null) return null;
        StringSerializer ss;
        if (isStringPrimitiveOrEnum())
        {
            return String.valueOf(val);
        }
        else if (StringSerializable.class.isAssignableFrom(getPropertyClass()))
        {
            return ((StringSerializable) val).writeString();
        }
        else if ((ss = m_classDatabase.getStringSerializer(getPropertyClass())) != null)
        {
            return ss.write(val);
        }
        return val.toString();
    }



    public boolean isImmutable()
    {
        return isStringPrimitiveOrEnum() || StringSerializable.class.isAssignableFrom(getMainType())
                || m_classDatabase.getStringSerializer(getMainType())!=null;
    }


    public static boolean objEquals(Object oldVal, Object newVal)
    {
        return oldVal == null && newVal == null || oldVal != null && oldVal.equals(newVal);
    }

    public Class getPropertyClass()
    {
        return m_class;
    }

    public String getName()
    {
        return m_propertyAccess.getName();
    }

    public boolean isReadOnly()
    {
        return m_propertyAccess.isReadOnly();
    }

    public boolean isReference()
    {
        return m_reference != null;
    }

    public Reference getReference()
    {
        return m_reference;
    }

    public boolean isStringOrPrimitive()
    {
        return isBoolean() || isInt() || isLong() || isDouble() || isFloat() || getMainType().isPrimitive() || getMainType().equals(String.class);
    }

    public boolean isStringPrimitiveOrEnum()
    {
        return isStringOrPrimitive() || getMainType().isEnum();
    }

    public Class getMainType() {
        return m_class;
    }

    public boolean isList()
    {
        return List.class.isAssignableFrom(m_class);
    }

    public boolean isSetCollection() {
        return Set.class.isAssignableFrom(m_class);
    }

    public boolean isCollection()
    {
        return m_class.isAssignableFrom(Collection.class);
    }

    public int compareTo(Object o)
    {
        Property other = (Property) o;
        Integer order1 =  m_propertyAccess.getOrdering();
        Integer order2 = other.m_propertyAccess.getOrdering();

        if (order1 == 0 && order2 == 0) //default to alphabetical
        {
            return getName().compareTo(other.getName());
        }

        return order1.compareTo(order2);
    }

    public boolean isMarshalAsAttribute()
    {
        return m_propertyAccess.getAnnotation(MarshalAsAttribute.class) != null;
    }

    public String toString()
    {
        return m_class.getSimpleName() + " " + StringUtils.decapitaliseFirst(getName());
    }

    public ClassModel<T> getPropertyClassModel()
    {
        return m_classDatabase.getClassModel(m_class);
    }

    public ClassModel getPropertyClassModel(Object instance)
    {
        Class aClass = instance.getClass();
        assert m_class.isAssignableFrom(aClass) : aClass.getSimpleName() + " not compatable with " + m_class.getSimpleName();
        return m_classDatabase.getClassModel(aClass);
    }

    public String getFilterOnProperty()
    {
        return m_filterOnProperty;
    }

    public boolean isSimpleType()
    {
        return m_classDatabase.isSimpleType(m_class);
    }

    public boolean isKey()
    {
        return key;
    }

    public boolean hasSpecialBoundComponent()
    {
        return m_editorWidget != null;
    }

    public String getBoundPropertyArgs()
    {
        return hasSpecialBoundComponent() ? m_editorWidget.args() : null;
    }

    public boolean isTransient()
    {
        return m_propertyAccess.isTransient();
    }

    /**
     * @return true if not transient OR transient AND displayNodes is set to true
     */
    public boolean isDisplayNodes()
    {
        return !m_propertyAccess.isTransient() || m_propertyAccess.displayNodes();
    }

    public ClassDatabase getClassDatabase()
    {
        return m_classDatabase;
    }

    public boolean isMarshalAsElement()
    {
        return m_propertyAccess.getAnnotation(MarshalAsElement.class) != null;
    }

    public boolean isBoolean()
    {
        return getMainType().equals(boolean.class) || getMainType().equals(Boolean.class);
    }

    public boolean isPrimitiveBoolean()
    {
        return getMainType().equals(boolean.class);
    }

    public boolean isFormattedText()
    {
        return m_formattedText;
    }

    public Class getParentClass()
    {
        return m_parentClass;
    }

    public String getQuery()
    {
        return m_query;
    }

    public boolean isEditable()
    {
        return m_editable && !isReadOnly();
    }

    public boolean isMandatory()
    {
        return m_mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        m_mandatory = mandatory;
    }

    public boolean isVisibilityRestricted()
    {
        return m_visibilityRestricted && !isHidden();
    }

    public boolean isDouble()
    {
        return getMainType() == double.class || getMainType() == Double.class;
    }

    public boolean isInt()
    {
        return getMainType() == int.class || getMainType() == Integer.class;
    }

    public boolean isFloat()
    {
        return getMainType() == float.class || getMainType() == Float.class;
    }

    public boolean isLong()
    {
        return getMainType() == long.class || getMainType() == Long.class;
    }

    public String getXMLMapping()
    {
        XMLMapping xmlMapping = m_propertyAccess.getAnnotation(XMLMapping.class);
        return xmlMapping!=null ? xmlMapping.value() : getName();
    }

	public boolean isEnum()
	{
		return getMainType().isEnum();
	}

    public boolean isHidden()
    {
        return m_propertyAccess.getAnnotation(Hide.class)!=null;
    }

    public EditorWidget getEditorWidget() {
        return m_editorWidget;
    }

    public Validate getValidate() {
        return m_propertyAccess.getAnnotation(Validate.class);
    }

    public boolean isComplexNonReference() {
        return !(isReference() || isImmutable() || isTransient());
    }

    public boolean isContainer() {
        return this instanceof ContainerProperty;
    }

    public void eachValue(ObjectMeta target, PropertyValueIterator propertyValueIterator) {
        T val = (T) target.get(this);
        if (val != null) {
            propertyValueIterator.exec(new ObjectLocation(target, this), 0, val);
        }
    }
}
