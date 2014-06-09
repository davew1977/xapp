package net.sf.xapp.objectmodelling.core;

/**
 * encapsulates the location of an object
 */
public class ObjRef {
    private final ObjectMeta parent;
    private final Property property;
    private final ObjectMeta ref;

    public ObjRef(ObjectMeta parent, Property property, ObjectMeta ref) {
        this.parent = parent;
        this.property = property;
        this.ref = ref;
    }

    public ObjectMeta getParent() {
        return parent;
    }

    public Property getProperty() {
        return property;
    }

    /**
     * nullify the underlying reference
     */
    public void dispose() {
        if (parent != null) {
            if(property.isContainer()) {
                ContainerProperty cp = (ContainerProperty) property;
                cp.remove(parent, ref);
            } else {
                parent.set(property, null);
            }
        }
    }

    /**
     * set the underlying reference (add if collection, put if map, set if one to one)
     */
    public void init() {
        if (parent!=null) {
            if(property.isContainer()) {
                ContainerProperty cp = (ContainerProperty) property;
                cp.add(parent, ref);
            } else {
                parent.set(property, this);
            }
        }
    }
}
