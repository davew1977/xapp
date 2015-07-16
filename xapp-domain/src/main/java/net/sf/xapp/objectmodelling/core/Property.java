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
import net.sf.xapp.annotations.marshalling.MarshalAsAttribute;
import net.sf.xapp.annotations.marshalling.MarshalAsElement;
import net.sf.xapp.annotations.marshalling.XMLMapping;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.TreeMeta;
import net.sf.xapp.marshalling.api.StringSerializable;
import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.utils.XappException;

import java.util.*;

import static net.sf.xapp.objectmodelling.core.NamespacePath.fullPath;

/**
 * A property encapsulates meta data about a class's property. A property is an attribute of a class that is
 * accessible through a getter and optionally modifiable through a setter
 */
public class Property<T> implements Comparable {
    protected final PropertyAccess propertyAccess;
    protected Class m_class;//class of return type of accessor
    private Reference reference;//true if this should point to another object in the datamodel
    private String filterOnProperty;
    private boolean key;
    private EditorWidgetFactory editorWidgetFactory; //override the default bound component
    protected ClassDatabase classDatabase;
    private boolean formattedText;
    private Class parentClass;
    private String query = "";
    private boolean editable;
    private boolean editableOnCreation;
    private List<PropertyChangeListener> listeners;
    private boolean mandatory;
    private boolean visibilityRestricted = true; //defaults to visible in dynamic GUI
    private boolean _transient;

    public Property(ClassModelManager classDatabase, PropertyAccess propertyAccess, Class aClass, Reference ref,
                    String filterOnProperty, Key key,
                    EditorWidget editorWidget,
                    boolean isformattedText,
                    Class parentClass,
                    String query,
                    boolean editable,
                    TreeMeta treeMeta,
                    boolean mandatory) {
        this.classDatabase = classDatabase;
        this.mandatory = mandatory;
        this.propertyAccess = propertyAccess;
        m_class = aClass;
        reference = ref;
        this.filterOnProperty = filterOnProperty;
        this.key = key != null;
        if(editorWidget != null) {
            this.editorWidgetFactory = new EditorWidgetFactory(editorWidget);
        }
        formattedText = isformattedText;
        this.parentClass = parentClass;
        this.query = query;
        this.editable = editable;
        editableOnCreation = editable;
        listeners = new ArrayList<PropertyChangeListener>();
        _transient = propertyAccess.isTransient();
    }

    public void setEditorWidgetType(Class type, Object... args) {
        this.editorWidgetFactory = new EditorWidgetFactory(type, args);
    }

    public PropertyAccess getPropertyAccess() {
        return propertyAccess;
    }

    public void addChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void setVisibilityRestricted(boolean visibilityRestricted) {
        this.visibilityRestricted = visibilityRestricted;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setEditableOnCreation(boolean editableOnCreation) {
        this.editableOnCreation = editableOnCreation;
    }

    public boolean isEditableOnCreation() {
        return editableOnCreation;
    }

    public Object get(Object target) {
        try {
            return propertyAccess.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("WARNING: property " + this + " in " + parentClass.getSimpleName() + " cannot be retrieved on " + target + " of class " + target.getClass().getSimpleName());
            return null;
        }
    }

    public RegularPropertyChange set(T target, Object newVal) {
        try {
            Object oldVal = get(target);
            if (!objEquals(oldVal, newVal)) {
                propertyAccess.set(target, newVal);
                for (PropertyChangeListener listener : listeners) {
                    listener.propertyChanged(this, target, oldVal, newVal);
                }
                return new RegularPropertyChange(this, target, oldVal, newVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("WARNING: property " + this + " in " + parentClass.getSimpleName() + " cannot be set on " + target + " of class " + target.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Set a property from a string value
     *
     * @param target
     * @param value
     */
    public void setSpecial(ObjectMeta<T> target, String value) {
        Object obj = convert(target, value);

        target.set(this, obj);
    }

    public String convert(ObjectMeta objectMeta, Object obj) {
        Class propertyClass = getPropertyClass();
        StringSerializer strSerializer = getClassDatabase().getStringSerializer(propertyClass);
        if(obj == null) {
            return null;
        }
        if(isStringPrimitiveOrEnum()) {
            return String.valueOf(obj);
        } else if(isReference()) {
            ClassModel classModel = getPropertyClassModel();
            ObjectMeta refObjMeta = classModel.find(obj);
            Namespace namespace = objectMeta.getNamespace(classModel);
            return fullPath(namespace, refObjMeta);
        } else if(strSerializer != null) {
            return strSerializer.write(obj);
        } else if(obj instanceof StringSerializable) {
            StringSerializable stringSerializable = (StringSerializable) obj;
            return stringSerializable.writeString();
        }
        throw new XappException("property "+ this+" is not string serializable");
    }

    public Object convert(ObjectMeta objectMeta, String value) {
        if(value == null) {
            return null;
        }
        Class propertyClass = getPropertyClass();
        StringSerializer strSerializer = getClassDatabase().getStringSerializer(propertyClass);
        Object obj = null;
        if (propertyClass.equals(String.class)) {
            obj = value;
        } else if (propertyClass.equals((boolean.class)) || propertyClass.equals(Boolean.class)) {
            obj = Boolean.valueOf(value);
        } else if (propertyClass.equals((int.class)) || propertyClass.equals(Integer.class)) {
            obj = new Integer(value);
        } else if (propertyClass.equals((short.class)) || propertyClass.equals(Short.class)) {
            obj = new Short(value);
        } else if (propertyClass.equals((byte.class)) || propertyClass.equals(Byte.class)) {
            obj = new Byte(value);
        } else if (propertyClass.equals((char.class)) || propertyClass.equals(Character.class)) {
            obj = value.charAt(0);
        } else if (propertyClass.equals((float.class)) || propertyClass.equals(Float.class)) {
            obj = new Float(value);
        } else if (propertyClass.equals((double.class)) || propertyClass.equals(Double.class)) {
            obj = new Double(value);
        } else if (propertyClass.equals((long.class)) || propertyClass.equals(Long.class)) {
            obj = new Long(value);
        } else if (propertyClass.isEnum()) {
            obj = Enum.valueOf(propertyClass, value);
        } else if (isReference()) {
            obj = objectMeta.get(propertyClass, value);
        } else if (strSerializer != null) {
            obj = strSerializer.read(value);
        } else if (StringSerializable.class.isAssignableFrom(propertyClass)) {
            StringSerializable o = null;
            try {
                o = (StringSerializable) propertyClass.newInstance();
            } catch (Exception e) {
                throw new XappException(e);
            }
            o.readString(value);
            obj = o;
        } else throw new XappException("property " + this + " is not StringSerializable");
        return obj;
    }

    public boolean isStringSerializable() {
        return StringSerializable.class.isAssignableFrom(getPropertyClass()) ||
                getClassDatabase().getStringSerializer(getPropertyClass()) != null;
    }

    public String toString(Object val) {
        if (val == null) return null;
        StringSerializer ss;
        if (isStringPrimitiveOrEnum()) {
            return String.valueOf(val);
        } else if (StringSerializable.class.isAssignableFrom(getPropertyClass())) {
            return ((StringSerializable) val).writeString();
        } else if ((ss = classDatabase.getStringSerializer(getPropertyClass())) != null) {
            return ss.write(val);
        }
        return val.toString();
    }


    public boolean isImmutable() {
        return isStringPrimitiveOrEnum() || isStringSerializable();
    }


    public static boolean objEquals(Object oldVal, Object newVal) {
        return oldVal == null && newVal == null || oldVal != null && oldVal.equals(newVal);
    }

    public Class getPropertyClass() {
        return m_class;
    }

    public String getName() {
        return propertyAccess.getName();
    }

    public boolean isReadOnly() {
        return propertyAccess.isReadOnly();
    }

    public boolean isReference() {
        return reference != null;
    }

    public Reference getReference() {
        return reference;
    }

    public boolean isStringOrPrimitive() {
        return isBoolean() || isInt() || isLong() || isDouble() || isFloat() || getMainType().isPrimitive() || getMainType().equals(String.class);
    }

    public boolean isStringPrimitiveOrEnum() {
        return isStringOrPrimitive() || getMainType().isEnum();
    }

    public Class getMainType() {
        return m_class;
    }

    public boolean isList() {
        return List.class.isAssignableFrom(m_class);
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(m_class);
    }

    public boolean isSetCollection() {
        return Set.class.isAssignableFrom(m_class);
    }

    public boolean isCollection() {
        return m_class.isAssignableFrom(Collection.class);
    }

    public int compareTo(Object o) {
        Property other = (Property) o;
        Integer order1 = propertyAccess.getOrdering();
        Integer order2 = other.propertyAccess.getOrdering();

        if (order1 == 0 && order2 == 0) //default to alphabetical
        {
            return getName().compareTo(other.getName());
        }

        return order1.compareTo(order2);
    }

    public boolean isMarshalAsAttribute() {
        return propertyAccess.getAnnotation(MarshalAsAttribute.class) != null;
    }

    public String toString() {
        return m_class.getSimpleName() + " " + StringUtils.decapitaliseFirst(getName());
    }

    public ClassModel<T> getPropertyClassModel() {
        return classDatabase.getClassModel(m_class);
    }

    public ClassModel getPropertyClassModel(Object instance) {
        Class aClass = instance.getClass();
        assert m_class.isAssignableFrom(aClass) : aClass.getSimpleName() + " not compatable with " + m_class.getSimpleName();
        return classDatabase.getClassModel(aClass);
    }

    public String getFilterOnProperty() {
        return filterOnProperty;
    }

    public boolean isSimpleType() {
        return classDatabase.isSimpleType(m_class);
    }

    public boolean isKey() {
        return key;
    }

    public boolean hasSpecialBoundComponent() {
        return editorWidgetFactory != null;
    }

    public String getBoundPropertyArgs() {
        return hasSpecialBoundComponent() ? editorWidgetFactory.args() : null;
    }

    public boolean isTransient() {
        return _transient;
    }

    public void setTransient(boolean _transient) {
        this._transient = _transient;
    }

    /**
     * @return true if not transient OR transient AND displayNodes is set to true
     */
    public boolean isDisplayNodes() {
        return !propertyAccess.isTransient() || propertyAccess.displayNodes();
    }

    public ClassDatabase getClassDatabase() {
        return classDatabase;
    }

    public boolean isMarshalAsElement() {
        return propertyAccess.getAnnotation(MarshalAsElement.class) != null;
    }

    public boolean isBoolean() {
        return getMainType().equals(boolean.class) || getMainType().equals(Boolean.class);
    }

    public boolean isPrimitiveBoolean() {
        return getMainType().equals(boolean.class);
    }

    public boolean isFormattedText() {
        return formattedText;
    }

    public Class getParentClass() {
        return parentClass;
    }

    public String getQuery() {
        return query;
    }

    public boolean isEditable() {
        return editable && !isReadOnly();
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public Property setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public boolean isVisibilityRestricted() {
        return visibilityRestricted && !isHidden();
    }

    public boolean isDouble() {
        return getMainType() == double.class || getMainType() == Double.class;
    }

    public boolean isInt() {
        return getMainType() == int.class || getMainType() == Integer.class;
    }

    public boolean isFloat() {
        return getMainType() == float.class || getMainType() == Float.class;
    }

    public boolean isLong() {
        return getMainType() == long.class || getMainType() == Long.class;
    }

    public String getXMLMapping() {
        XMLMapping xmlMapping = propertyAccess.getAnnotation(XMLMapping.class);
        return xmlMapping != null ? xmlMapping.value() : getName();
    }

    public boolean isEnum() {
        return getMainType().isEnum();
    }

    public boolean isHidden() {
        return propertyAccess.getAnnotation(Hide.class) != null;
    }

    public Validate getValidate() {
        return propertyAccess.getAnnotation(Validate.class);
    }

    public boolean isComplexNonReference() {
        return !(isReference() || isImmutable() || isTransient());
    }

    public boolean canConvertToString(){
        return isReference() || isImmutable() || isTransient();
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

    public ClassModel getMainTypeClassModel() {
        return getClassDatabase().getClassModel(getMainType());
    }

    public Property setKey(boolean b) {
        key = b;
        return this;
    }


    public Object createEditorWidget() {
        return editorWidgetFactory.create();
    }

    public Property setPropOrder(int order) {
        propertyAccess.setOrdering(order);
        return this;
    }
}
