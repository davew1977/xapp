package net.sf.xapp.objectmodelling.core;

/**
 * encapsulates the location of an object
 */
public class ObjRef {
    private final ObjectMeta obj;
    private final Property property;

    public ObjRef(ObjectMeta parent, Property property) {
        this.obj = parent;
        this.property = property;
    }

    public ObjectMeta getObj() {
        return obj;
    }

    public Property getProperty() {
        return property;
    }
}
