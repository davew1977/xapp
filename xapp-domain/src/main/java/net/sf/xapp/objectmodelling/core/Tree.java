package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * provides some useful tree related functionality
 */
public class Tree<T extends Tree> extends TreeNode<T> {

    public Tree(Class<T> type) {
        super(type);
    }

    public Tree(Class<T> type, String name) {
        super(type, name);
    }

    public List<T> children() {
        return treeContext.children(type);
    }

    public List<T> enumerate() {
        return enumerate(type);
    }

    public <E> List<E> enumerate(Class<E> filterClass) {
        return treeContext.enumerate(filterClass);
    }

    public <E> List<E> enumerate(Class<E> filterClass, Filter<? super E> filter) {
        return treeContext.enumerate(filterClass, filter);
    }

    public T getChild(String name) {
        return getChild(type, name);
    }

    public <E> E getChild(Class<E> filterClass, String name) {
        return treeContext.child(filterClass, name);
    }

    /**
     * the path of this node in the notional tree
     */
    public String pathKey() {
        List<? extends T> path = path();
        List<ObjectMeta> objectMetas = new ArrayList<>();
        for (T t : path) {
           objectMetas.add(t.treeContext.objMeta());
        }
        return StringUtils.join(objectMetas, ".");
    }

    @Transient
    public boolean isLeaf() {
        return children().isEmpty();
    }

}
