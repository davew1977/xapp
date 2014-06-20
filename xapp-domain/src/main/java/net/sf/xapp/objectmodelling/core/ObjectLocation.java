package net.sf.xapp.objectmodelling.core;

import java.util.Collection;

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

    public void unset(ObjectMeta ref) {
        if (property.isContainer()) {
            ContainerProperty cp = (ContainerProperty) property;
            cp.remove(obj, ref);
        } else {
            obj.set(property, null);
        }
    }

    public void set(ObjectMeta ref) {
        set(ref, -1);
    }
    public void set(ObjectMeta ref, int index) {
        if (property.isContainer()) {
            ContainerProperty cp = (ContainerProperty) property;
            cp.add(obj, index, ref);
        } else {
            obj.set(property, ref.getInstance());
        }
    }

    public boolean isCollection() {
        return property.isContainer();
    }

    public boolean containsReferences() {
        return ((ContainerProperty) property).containsReferences();
    }

    public void updateIndex(ObjectMeta objectMeta, int index) {
        unset(objectMeta);
        set(objectMeta, index);
    }

    public Collection getCollection() {
        return ((ContainerProperty) property).getCollection(obj);
    }
}
