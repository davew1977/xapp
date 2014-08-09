package net.sf.xapp.objectmodelling.core;

import java.util.Collection;
import java.util.Map;

/**
 * encapsulates the location of an object
 */
public class ObjectLocation {
    private final ObjectMeta obj;
    private final Property property;

    public ObjectLocation(ObjectMeta obj, Property property) {
        this.obj = obj;
        this.property = property;
    }

    public ObjectMeta getObj() {
        return obj;
    }

    public Property getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectLocation objectLocation = (ObjectLocation) o;

        return obj.equals(objectLocation.obj) && property.equals(objectLocation.property);

    }

    @Override
    public int hashCode() {
        int result = obj.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }

    public PropertyChange unset(ObjectMeta ref) {
        if (property.isContainer()) {
            ContainerProperty cp = (ContainerProperty) property;
            cp.remove(obj, ref);
            return null;
        } else {
            return obj.set(property, null);
        }
    }

    public PropertyChange set(ObjectMeta ref) {
        return set(ref, -1);
    }
    public PropertyChange set(ObjectMeta ref, int index) {
        if (property.isContainer()) {
            ContainerProperty cp = (ContainerProperty) property;
            return cp.add(obj, index, ref);
        } else {
            return obj.set(property, ref.getInstance());
        }
    }

    public boolean isCollection() {
        return property.isContainer();
    }

    public boolean containsReferences() {
        return ((ContainerProperty) property).containsReferences();
    }

    public int adjustIndex(ObjectMeta objectMeta, int delta) {
        int currentIndex = indexOf(objectMeta);
        int newIndex = currentIndex + delta;
        return setIndex(objectMeta, newIndex);
    }

    public int setIndex(ObjectMeta objectMeta, int index) {
        unset(objectMeta);
        set(objectMeta, index);
        return index;
    }

    public int indexOf(ObjectMeta objectMeta) {
        ContainerProperty cp = (ContainerProperty) property;
        return cp.indexOf(obj, objectMeta);
    }

    public Collection getCollection() {
        return ((ContainerProperty) property).getCollection(obj.getInstance());
    }

    public boolean isList() {
        return property.isList();
    }

    public int size() {
        return isCollection() ? ((ContainerProperty) property).size(obj) : 1;
    }

    @Override
    public String toString() {
        return String.format("%s %s.%s", obj.getSimpleClassName(), obj.getKey(), property);
    }

    private String toStringSuffix(ObjectMeta objectMeta){
        return isList() ? "["+indexOf(objectMeta)+"]" : "";
    }

    public String toString(ObjectMeta objectMeta) {
        return toString() + toStringSuffix(objectMeta);
    }

    public ClassModel getPropClassModel() {
        return isCollection() ? ((ContainerProperty) property).getContainedTypeClassModel() : property.getPropertyClassModel();
    }

    public Class getPropertyClass() {
        return property.getMainType();
    }

    public boolean isMap() {
        return property.isMap();
    }

    public void keyChanged(String oldVal, String newVal) {
        assert isMap();
        ContainerProperty cp = (ContainerProperty) property;
        Map map = (Map) cp.get(obj.getInstance());
        Object item = map.remove(String.valueOf(oldVal));
        map.put(String.valueOf(newVal), item);
    }

    public boolean isReference() {
        return getProperty().isReference();
    }

    public void set(String strValueOfProperty) {
        property.setSpecial(obj, strValueOfProperty);
    }

    public Namespace getNamespace() {
        return obj.getNamespace(getPropertyClass());
    }

    public void addPendingRef(String key) {
        getNamespace().addPendingRef(this, key);
    }
}
