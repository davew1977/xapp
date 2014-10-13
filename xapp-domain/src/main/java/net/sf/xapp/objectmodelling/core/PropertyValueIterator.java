package net.sf.xapp.objectmodelling.core;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public interface PropertyValueIterator {
    void exec(ObjectLocation objectLocation, int index, Object value);
}
