package net.sf.xapp.objectmodelling.core;

import java.util.List;

/**
 * simple facade for managed object to query its position in a tree
 */
public interface TreeContext {
    <X> X parent(Class<X> matchingType);
    <X> List<X> path(Class<X> matchingType);
    <X> List<X> children(Class<X> matchingType);
    <X> List<X> enumerate(Class<X> filterClass);

    ObjectMeta objMeta();
}
