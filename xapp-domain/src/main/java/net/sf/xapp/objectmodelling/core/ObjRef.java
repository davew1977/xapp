package net.sf.xapp.objectmodelling.core;

/**
 * encapsulates the location of an object
 */
public class ObjRef {
    private final ObjectLocation objectLocation;
    private final ObjectMeta ref;

    public ObjRef(ObjectLocation objectLocation, ObjectMeta ref) {
        this.objectLocation = objectLocation;
        this.ref = ref;
    }

    public ObjectLocation getObjectLocation() {
        return objectLocation;
    }

    public ObjectMeta getParent() {
        return objectLocation.getObj();
    }

    public Property getProperty() {
        return objectLocation.getProperty();
    }

    /**
     * nullify the underlying reference
     */
    public void unset() {
        if (objectLocation != null) {
            objectLocation.unset(ref);
        }
    }

    /**
     * set the underlying reference (add if collection, put if map, set if one to one)
     */
    public void set() {
        if (objectLocation != null) {
            objectLocation.set(ref);
        }
    }
}
