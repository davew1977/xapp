package net.sf.xapp.objectmodelling.core;

/**
 * encapsulates the location of an object
 */
public class ObjectLocation {
    private final ObjectMeta obj;
    private final Property property;
    private int index;
    private Object attachment;

    public ObjectLocation(ObjectMeta obj, Property property) {
        this(obj, property, -1);
    }
    public ObjectLocation(ObjectMeta obj, Property property, int index) {
        this.obj = obj;
        this.property = property;
        this.index = index;
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
        if (property.isContainer()) {
            ContainerProperty cp = (ContainerProperty) property;
            cp.add(obj, index, ref);
        } else {
            obj.set(property, ref.getInstance());
        }
    }

    /*public Object getAttachment() {
        return attachment;
    }*/

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public boolean isCollection() {
        return property.isContainer();
    }

    public int index() {
        return index;
    }

    public boolean containsReferences() {
        return ((ContainerProperty) property).containsReferences();
    }

    public void updateIndex(ObjectMeta objectMeta, int index) {
        unset(objectMeta);
        this.index = index;
        set(objectMeta);
    }
}
