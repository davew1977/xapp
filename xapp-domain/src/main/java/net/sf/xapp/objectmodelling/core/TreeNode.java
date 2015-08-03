package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.PreInit;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * provides some useful tree related functionality
 */
public class TreeNode<T extends TreeNode> {
    @Key
    protected String name;
    protected transient TreeContext treeContext;
    protected transient Class<T> type;

    public TreeNode(Class<T> type) {
        this.type = type;
    }

    public TreeNode(Class<T> type, String name) {
        this(type);
        this.name = name;

    }

    @PreInit
    public void preInit(TreeContext treeContext) {
        this.treeContext = treeContext;
    }

    public T parent() {
        return treeContext.parent(type);
    }
    public <X> X parent(Class<X> type) {
        return treeContext.parent(type);
    }
    public <X> X ancestor(Class<X> type) {
        return treeContext.ancestor(type);
    }

    public List<? extends T> path() {
        return treeContext.path(type);
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

    @Transient
    public boolean isLeaf() {
        return children().isEmpty();
    }
    @Override
    public String toString() {
        return name;
    }

    @Transient
    public boolean isRoot() {
        return parent() == null;
    }


    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <X> ObjectMeta<X> objMeta() {
        return treeContext.objMeta();
    }

}
