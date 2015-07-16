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
public class AbstractNode<T extends AbstractNode> {
    @Key
    protected String name;
    protected transient TreeContext treeContext;
    protected transient Class<T> type;

    public AbstractNode(Class<T> type) {
        this.type = type;
    }

    public AbstractNode(Class<T> type, String name) {
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

    public List<? extends T> path() {
        return treeContext.path(type);
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

    @Override
    public String toString() {
        return name;
    }

    @Transient
    public boolean isRoot() {
        return parent() == null;
    }

    @Transient
    public boolean isLeaf() {
        return children().isEmpty();
    }

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
