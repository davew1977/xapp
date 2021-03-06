package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.application.Container;

import java.util.ArrayList;
import java.util.List;

/**
 * provides some useful tree related functionality
 */
//@NamespaceFor(TreeNode.class)
@Container(listProperty = "children")
public class Tree<T extends TreeNode> extends TreeNode<T> {
    protected List<T> children = new ArrayList<>();

    public Tree() {
        super((Class<T>) Tree.class);
    }

    public Tree(Class<T> type) {
        super(type);
    }

    public Tree(Class<T> type, String name) {
        super(type, name);
    }

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

}
