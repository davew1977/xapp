package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.utils.Filter;

import java.util.List;

/**
 * simple facade for managed object to query its position in a tree
 */
public interface TreeContext {
    <X> X parent(Class<X> matchingType);
    <X> X ancestor(Class<X> matchingType);
    <X> List<X> path(Class<X> matchingType);
    <X> List<X> children(Class<X> matchingType);
    <X> X child(Class<X> matchingType, String name);
    <X> List<X> enumerate(Class<X> filterClass);
    <X> List<X> enumerate(Class<X> filterClass, boolean includeSelf);
    <X> List<X> enumerate(Class<X> filterClass, Filter<? super X> filter);

    <X> ObjectMeta<X> objMeta();
}
