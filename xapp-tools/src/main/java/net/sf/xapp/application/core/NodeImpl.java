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
package net.sf.xapp.application.core;

import net.sf.xapp.application.api.*;
import net.sf.xapp.application.commands.RefreshCommand;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.tree.Tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node
{
    protected ApplicationContainer m_applicationContainer;
    protected Tree m_domainTreeRoot;
    protected DefaultMutableTreeNode m_jtreeNode;
    private ListNodeContext m_listNodeContext;
    private ObjectNodeContext m_objectNodeContext;

    public NodeImpl(ApplicationContainer applicationContainer, Tree domainTreeRoot, DefaultMutableTreeNode jtreeNode, ListNodeContext listNodeContext, ObjectNodeContext objectNodeContext)
    {
        m_applicationContainer = applicationContainer;
        m_domainTreeRoot = domainTreeRoot;
        m_jtreeNode = jtreeNode;
        m_listNodeContext = listNodeContext;
        m_objectNodeContext = objectNodeContext;
    }

    public DefaultMutableTreeNode getJtreeNode()
    {
        return m_jtreeNode;
    }

    public void setJtreeNode(DefaultMutableTreeNode jtreeNode)
    {
        m_jtreeNode = jtreeNode;
    }

    public boolean isRoot()
    {
        return m_jtreeNode != null && m_jtreeNode.isRoot();
    }

    public Node getParent()
    {
        DefaultMutableTreeNode parentJTreeNode = (DefaultMutableTreeNode) m_jtreeNode.getParent();
        //parent jtree node can be null if the node was not added to jtree (it was filtered out)
        return parentJTreeNode != null ? (Node) parentJTreeNode.getUserObject() : null;
    }

    public Node getChildBefore(Node node)
    {
        DefaultMutableTreeNode childBefore = (DefaultMutableTreeNode) m_jtreeNode.getChildBefore(node.getJtreeNode());
        return childBefore != null ? (Node) childBefore.getUserObject() : null;
    }

    @Override
    public Node getChildAfter(Node node)
    {
        DefaultMutableTreeNode childAfter = (DefaultMutableTreeNode) m_jtreeNode.getChildAfter(node.getJtreeNode());
        return childAfter != null ? (Node) childAfter.getUserObject() : null;
    }

    public Node getChildAt(int index)
    {
        return (Node) ((DefaultMutableTreeNode) m_jtreeNode.getChildAt(index)).getUserObject();
    }

    public ObjectNodeContext getObjectNodeContext()
    {
        return m_objectNodeContext;
    }

    public ListNodeContext getListNodeContext()
    {
        return m_listNodeContext;
    }

    @Override
    public <T> boolean isA(Class<T> aClass)
    {
        return aClass.isInstance(wrappedObject()); 
    }

    public Object wrappedObject()
    {
        return m_objectNodeContext != null ? m_objectNodeContext.getInstance() : null;
    }

    public Object nearestWrappedObject()
    {
        return m_objectNodeContext != null ? m_objectNodeContext.getInstance() : getParent().wrappedObject();
    }

    public ApplicationContainer getApplicationContainer()
    {
        return m_applicationContainer;
    }

    public int indexOf(Node node)
    {
        return m_jtreeNode.getIndex(node.getJtreeNode());
    }

    public int numChildren()
    {
        return m_jtreeNode.getChildCount();
    }

    public TreePath getPath()
    {
        return new TreePath(m_jtreeNode.getPath());
    }

    //APPLICATION API
    public Tree getDomainTreeRoot()
    {
        return m_domainTreeRoot;
    }

    public List<Command> createCommands(CommandContext commandContext)
    {
        ArrayList<Command> commands = new ArrayList<Command>();
        if (m_listNodeContext != null) commands.addAll(m_listNodeContext.createCommands(this, commandContext));
        if (m_objectNodeContext != null) commands.addAll(m_objectNodeContext.createCommands(this, commandContext));
        if (commandContext != CommandContext.SEARCH) commands.add(new RefreshCommand());

        return commands;
    }

    public String toString()
    {
        //special root handling, default to file name minus suffix
        if (isRoot() && !m_objectNodeContext.hasToStringMethod())
        {
			File currentFile = m_applicationContainer.getGuiContext().getCurrentFile();
			if(currentFile != null)
			{
				String filename = currentFile.getName();
				return filename.substring(0, filename.lastIndexOf("."));
			}
			else
			{
				return m_objectNodeContext.getClassModel().getContainedClass().getSimpleName();
			}
		}
        if (wrappedObject() != null)
        {
            if(m_objectNodeContext.hasToStringMethod()) {
                String strValue = wrappedObject().toString();
                if (strValue != null && strValue.length() > 50)
                {
                    strValue = strValue.substring(0, 50);
                }
                return strValue;
            } else {
                Property property = m_objectNodeContext.getProperty();
                return wrappedObject().getClass().getSimpleName() + (property != null ? ":" + property.getName() : "");
            }
        }
        return m_listNodeContext.getContainerProperty().getName();
    }

    public boolean containsReferences()
    {
        return m_listNodeContext != null && m_listNodeContext.getContainerProperty().containsReferences();
    }

    public boolean isReference()
    {
        return !isRoot() && getParent().containsReferences();
    }

    public void updateDomainTreeRoot()
    {
        if (m_domainTreeRoot != null)
        {
            m_domainTreeRoot.updateTree();
        }
    }

    public boolean canEdit()
    {
        return m_objectNodeContext != null && m_objectNodeContext.canEdit();
    }

    @Override
    public int index() {
        return getParent().indexOf(this);
    }
}