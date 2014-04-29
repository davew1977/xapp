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

import net.sf.xapp.annotations.application.Mandatory;
import net.sf.xapp.annotations.application.NotEditable;
import net.sf.xapp.annotations.marshalling.PropertyOrder;
import net.sf.xapp.annotations.objectmodelling.GlobalKey;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ValidImplementations(Tree.class)
public abstract class TreeNode implements Comparable, Cloneable
{
    private String m_key = "";
    private String m_name;
    private Tree m_parent;

    protected TreeNode()
    {
    }

    protected TreeNode(String name) {
        this.m_name = name;
    }

    /**
     * 20131025 renamed to parent to prevent other java binding technologies from treating it as a property
     * @return the parent Tree node
     */
    public Tree parent()
    {
        return m_parent;
    }

    public void setParent(Tree parent)
    {
        m_parent = parent;
    }


    @GlobalKey
    @NotEditable
    @Transient
    public String getKey()
    {
        return m_key;
    }

    public final void setKey(String key)
    {
        m_key = key;
    }

    @PropertyOrder(-1)
    @Mandatory
    public String getName()
    {
        return m_name;
    }

    public final void setName(String name)
    {
        m_name = name;
    }

    public final String resolveKey(String pathSeparator)
    {
        return resolvePath(pathSeparator) + getName();
    }

    public final String resolvePath(String pathseparator)
    {
        List<TreeNode> nodes = resolvePath();
        StringBuilder sb = new StringBuilder();
        for (TreeNode node : nodes)
        {
            sb.append(node.getName()).append(pathseparator);
        }
        return sb.toString();
    }

    public final List<TreeNode> resolvePath()
    {
        LinkedList<TreeNode> path = new LinkedList<TreeNode>();
        Tree parent = parent();
        while (parent != null)
        {
            path.addFirst(parent);
            parent = parent.parent();
        }
        return path;
    }

    public int compareTo(Object o)
    {
        return m_key.compareTo(((TreeNode) o).m_key);
    }

    public TreeNode clone() throws CloneNotSupportedException
    {
        TreeNode treeNode = (TreeNode) super.clone();
        //reset key
        treeNode.m_key = "";
        return treeNode;
    }

    public String toString()
    {
        return m_name;
    }

    public List<TreeNode> search(String path)
    {
        return new ArrayList<TreeNode>();
    }

    public TreeNode find(String path)
    {
        return m_name.equals(path) ? this : null;
    }

    public boolean isChildOf(Tree tree)
    {
        if (tree == null) return false;
        return tree.getChildren().contains(this);
    }


}
