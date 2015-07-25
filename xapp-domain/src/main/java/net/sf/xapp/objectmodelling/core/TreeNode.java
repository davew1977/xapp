package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.PreInit;
import net.sf.xapp.annotations.objectmodelling.Transient;
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
        return true;
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
