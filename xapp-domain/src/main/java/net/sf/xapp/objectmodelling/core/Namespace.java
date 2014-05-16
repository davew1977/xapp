package net.sf.xapp.objectmodelling.core;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface Namespace {
    <E> ObjectMeta<E> find(Class<E> aClass, String path);
}
