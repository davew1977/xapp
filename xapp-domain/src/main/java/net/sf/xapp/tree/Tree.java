/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @deprecated use net.sf.xapp.objectmodelling.core.Tree instead
 */
@Deprecated
public class Tree extends TreeNode
{
    public static final int INCLUDE_LEAVES = 0x01;
    public static final int INCLUDE_NODES = 0x02;
    public static final int RECURSIVE = 0x04;

    private List<TreeNode> m_children;
    private String m_pathSeparator = ".";
    private Class[] m_leafTypes;

    public Tree()
    {
        m_children = new ArrayList<TreeNode>();
    }

    public Tree(String name) {
        super(name);
    }

    public final List<TreeNode> getChildren()
    {
        return m_children;
    }

    public final void setChildren(List<TreeNode> children)
    {
        m_children = children;
    }

    public final void updateTree()
    {
        updateTree(m_pathSeparator);
    }

    public final void updateTree(String pathseparator)
    {
        m_pathSeparator = pathseparator;

        if (m_children != null)
        {
            for (TreeNode treeNode : m_children)
            {
                treeNode.setParent(this);
                if (treeNode instanceof Tree)
                {
                    ((Tree) treeNode).updateTree(pathseparator);
                }
                //null parent means we are root node
                treeNode.setKey(treeNode.resolveKey(pathseparator));
            }
        }
        if (_isRoot()) setKey(getName());
    }

    public void setPathSeparator(String pathSeparator)
    {
        m_pathSeparator = pathSeparator;
    }

    public final String pathSeparator()
    {
        return m_pathSeparator;
    }

    public final String pathSeparatorRegExp()
    {
        return m_pathSeparator.equals(".") ? "\\." : m_pathSeparator;
    }

    public List enumerate()
    {
        return enumerate(INCLUDE_LEAVES | INCLUDE_NODES | RECURSIVE);
    }

    public List enumerate(int options)
    {
        return enumerate((options & INCLUDE_LEAVES) != 0, (options & INCLUDE_NODES) != 0, (options & RECURSIVE) != 0);
    }

    public List enumerate(boolean includeLeaves, boolean includeNodes, boolean recursive)
    {
        ArrayList<TreeNode> list = new ArrayList<TreeNode>();
        if (includeNodes) list.add(this);
        if (m_children != null)
        {
            for (TreeNode treeNode : m_children)
            {
                if (treeNode instanceof Tree && ((Tree) treeNode).hasChildren())
                {
                    if (recursive) list.addAll(((Tree) treeNode).enumerate(includeLeaves, includeNodes, recursive));
                }
                else
                {
                    if (includeLeaves) list.add(treeNode);
                }
            }
        }
        return list;
    }

    public <T> List<T> enumerate(Class<T> filter)
    {
        ArrayList<T> list = new ArrayList<T>();
        if (m_children != null)
        {
            for (TreeNode treeNode : m_children)
            {
                if (treeNode instanceof Tree)
                {
                    list.addAll(((Tree) treeNode).enumerate(filter));
                }
                if (filter.isAssignableFrom(treeNode.getClass()))
                {
                    list.add((T) treeNode);
                }
            }
        }
        return list;
    }

    public boolean hasChildren()
    {
        return m_children != null && !m_children.isEmpty();
    }

    /**
     * Retrieves the next child. Will loop back to first if node is th last
     *
     * @param node
     * @return
     */
    public TreeNode nextChild(TreeNode node)
    {
        List<TreeNode> children = getChildren();
        return children.get((children.indexOf(node) + 1) % children.size());
    }

    public TreeNode previousChild(TreeNode node)
    {
        List<TreeNode> children = getChildren();
        return children.get((children.indexOf(node) - 1 + children.size()) % children.size());
    }


    public TreeNode clone() throws CloneNotSupportedException
    {
        Tree clone = (Tree) super.clone();
        clone.m_children = new ArrayList<TreeNode>();
        for (TreeNode child : m_children)
        {
            clone.m_children.add(child.clone());
        }
        return clone;
    }

    public TreeNode childAt(int index)
    {
        return index > getChildren().size() - 1 ? null : getChildren().get(index);
    }

    public int numChildren()
    {
        return getChildren().size();
    }

    /**
     * 20131025 added '_' prefix to prevent other java binding technologies from treating it as a property
     * @return true if root
     */
    public boolean _isRoot()
    {
        return parent() == null;
    }

    /**
     * 20131025 renamed to root to prevent other java binding technologies from treating it as a property
     * @return the root node
     */
    public Tree root()
    {
        if (_isRoot()) return this;
        return parent().root();
    }

    /**
     * 20131025 renamed to depth to prevent other java binding technologies from treating it as a property
     * @return the depth of this node in the tree; root = 0
     */
    public int depth()
    {
        return treePath().size() - 1;
    }

    public List<TreeNode> treePath()
    {
        List<TreeNode> path = _isRoot() ? new ArrayList<TreeNode>() : parent().treePath();
        path.add(this);
        return path;
    }

    /**
     * 20131025 added '_' prefix to prevent other java binding technologies from treating it as a property
     * @return true if node is a leaf
     */
    public boolean _isLeaf()
    {
        return !hasChildren();
    }

    public TreeNode find(String path)
    {
        if (path.contains(pathSeparator()))
        {
            String[] s = path.split(pathSeparatorRegExp(), 2);
            String head = s[0];
            String tail = s[1];
            //find child with name matching head
            for (TreeNode child : m_children)
            {
                if (child.getName().equals(head)) return child.find(tail);
            }
            return null;
        }
        else
        {
            return getChild(path);
        }
    }

    public TreeNode getChild(String name)
    {
        //TODO consider replacing m_children with a TreeNodeList that can also act as a map
        for (TreeNode child : m_children)
        {
            if (child.getName().equals(name)) return child;
        }
        return null;
    }

    /**
     * Search this node for child nodes and properties whose name matches regexp
     *
     * @param path the match pattern
     * @return list of both TreeNodes and PropertyObjectPairs
     */
    public List<TreeNode> search(String path)
    {
        List<TreeNode> results = new ArrayList<TreeNode>();
        if (path.contains(pathSeparator()))
        {
            String[] s = path.split(pathSeparatorRegExp(), 2);
            String head = s[0];
            String tail = s[1];
            TreeNode treeNode = getChild(head);
            if (treeNode == null) return results;
            results.addAll(treeNode.search(tail));
            return results;
        }
        else
        {
            for (TreeNode child : m_children)
            {
                if (child.getName().startsWith(path)) results.add(child);
            }
            results.addAll(super.search(path));
            return results;
        }
    }

    /**
     * Recursive sort in alphabetic order.
     */
    public void sort(final boolean ascending)
    {

        Collections.sort(getChildren(), new Comparator<TreeNode>()
        {
            public int compare(TreeNode node1, TreeNode node2)
            {
                if (ascending)
                    return node1.getName().compareTo(node2.getName());
                return -node1.getName().compareTo(node2.getName());
            }
        });

        for (TreeNode node : getChildren())
        {
            if (node instanceof Tree)
            {
                ((Tree) node).sort(ascending);
            }
        }
    }

    public void setLeafTypes(Class[] leafTypes)
    {
        m_leafTypes = leafTypes;
    }

    public final Class[] leafTypes()
    {
        return m_leafTypes;
    }



    /**
     * 20131025 added '_' prefix to prevent other java binding technologies from treating it as a property
     * @param node
     * @return true if node is a child of or is a descendent of this node
     */
    public boolean _isDescendant(TreeNode node)
    {
        for (TreeNode child : m_children)
        {
            if(node==child) return true;
            if(child instanceof Tree && ((Tree) child)._isDescendant(node)) return true;
        }
        return false;
    }

    public <T> T step(int delta, T thisContent, Class<T> filterClass)
    {
        List<T> contents = enumerate(filterClass);
        int index = contents.indexOf(thisContent) + delta;
        return contents.get(index == -1 ? contents.size() - 1 : index == contents.size() ? 0 : index);
    }
}
