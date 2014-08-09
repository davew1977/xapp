package net.sf.xapp.objectmodelling.core;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 09/08/2014
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class PendingObjectReference {
    private final ObjectLocation targetLocation;
    private final String key;

    public PendingObjectReference(ObjectLocation targetLocation, String key) {
        this.targetLocation = targetLocation;
        this.key = key;
    }

    public ObjectLocation getTargetLocation() {
        return targetLocation;
    }

    public String getKey() {
        return key;
    }
}
