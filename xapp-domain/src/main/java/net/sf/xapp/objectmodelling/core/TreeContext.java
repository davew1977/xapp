package net.sf.xapp.objectmodelling.core;

import java.util.List;

/**
 * simple facade for managed object to query its position in a tree
 */
public interface TreeContext<T> {
    T parent();
    List<T> path();
    List<T> children();
    String pathId();

    <E> List<E> enumerate(Class<E> filterClass);
}
