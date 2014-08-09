package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.marshalling.Unmarshaller;

import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface Namespace {
    <E> ObjectMeta<E> find(Class<E> aClass, String path);
    <E> Map<String, ObjectMeta<E>> all(Class<E> aClass);
    void addPendingRef(ObjectLocation targetLocation, String key);
}
