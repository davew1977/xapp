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
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node
{
    protected ApplicationContainer appContainer;
    protected DefaultMutableTreeNode jTreeNode;
    private ListNodeContext listNodeContext;
    private ObjectNodeContext objectNodeContext;

    public NodeImpl(ApplicationContainer applicationContainer, DefaultMutableTreeNode jtreeNode, ListNodeContext listNodeContext, ObjectNodeContext objectNodeContext)
    {
        appContainer = applicationContainer;
        jTreeNode = jtreeNode;
        this.listNodeContext = listNodeContext;
        this.objectNodeContext = objectNodeContext;
    }

    public DefaultMutableTreeNode getJtreeNode()
    {
        return jTreeNode;
    }

    public void setJtreeNode(DefaultMutableTreeNode jtreeNode)
    {
        jTreeNode = jtreeNode;
    }

    public boolean isRoot()
    {
        return jTreeNode != null && jTreeNode.isRoot();
    }

    public Node getParent()
    {
        DefaultMutableTreeNode parentJTreeNode = (DefaultMutableTreeNode) jTreeNode.getParent();
        //parent jtree node can be null if the node was not added to jtree (it was filtered out)
        return parentJTreeNode != null ? (Node) parentJTreeNode.getUserObject() : null;
    }

    public Node getChildBefore(Node node)
    {
        DefaultMutableTreeNode childBefore = (DefaultMutableTreeNode) jTreeNode.getChildBefore(node.getJtreeNode());
        return childBefore != null ? (Node) childBefore.getUserObject() : null;
    }

    @Override
    public Node getChildAfter(Node node)
    {
        DefaultMutableTreeNode childAfter = (DefaultMutableTreeNode) jTreeNode.getChildAfter(node.getJtreeNode());
        return childAfter != null ? (Node) childAfter.getUserObject() : null;
    }

    public Node getChildAt(int index)
    {
        return (Node) ((DefaultMutableTreeNode) jTreeNode.getChildAt(index)).getUserObject();
    }

    public ObjectNodeContext getObjectNodeContext()
    {
        return objectNodeContext;
    }

    public ListNodeContext getListNodeContext()
    {
        return listNodeContext;
    }

    @Override
    public <T> boolean isA(Class<T> aClass)
    {
        return aClass.isInstance(wrappedObject()); 
    }

    public Object wrappedObject()
    {
        return objectNodeContext != null ? objectNodeContext.instance():  null;
    }

    public Object nearestWrappedObject()
    {
        return objectNodeContext != null ? objectNodeContext.instance() : getParent().wrappedObject();
    }

    public Node parentObjectNode() {
        return getParent().getObjectNodeContext() != null ? getParent() : getParent().parentObjectNode();
    }

    public ObjectMeta parentObjectMeta() {
        return parentObjectNode().objectMeta();
    }

    @Override
    public ObjectMeta objectMeta() {
        return objectNodeContext != null ? getObjectNodeContext().objectMeta() : getParent().objectMeta();
    }

    @Override
    public void refresh() {
        getAppContainer().refreshNode(this);
    }

    @Override
    public ObjectLocation newObjLocation() {
        return newObjLocation(numChildren());
    }

    @Override
    public ObjectLocation newObjLocation(int index) {
        ObjectLocation objectLocation = new ObjectLocation(objectMeta(), getListNodeContext().getContainerProperty(), index);
        objectLocation.setAttachment(this);
        return objectLocation;
    }

    @Override
    public ObjectLocation thisObjLocation() {
        assert objectNodeContext != null;
        return getParent().newObjLocation(index());
    }

    @Override
    public ClassDatabase getClassDatabase() {
        return getAppContainer().getClassDatabase();
    }

    @Override
    public void updateIndex(int newIndex) {
        int oldIndex = index();
        Node parentNode = getParent();

        DefaultTreeModel treeModel = (DefaultTreeModel) getAppContainer().getMainTree().getModel();
        treeModel.removeNodeFromParent(getJtreeNode());
        treeModel.insertNodeInto(getJtreeNode(), parentNode.getJtreeNode(), newIndex);
        if(newIndex>oldIndex) {
            appContainer.getApplication().nodeMovedDown(this);
        } else {
            appContainer.getApplication().nodeMovedUp(this);
        }
    }

    public ApplicationContainer getAppContainer()
    {
        return appContainer;
    }

    public int indexOf(Node node)
    {
        return jTreeNode.getIndex(node.getJtreeNode());
    }

    public int numChildren()
    {
        return jTreeNode.getChildCount();
    }

    public TreePath getPath()
    {
        return new TreePath(jTreeNode.getPath());
    }

    public List<Command> createCommands(CommandContext commandContext)
    {
        ArrayList<Command> commands = new ArrayList<Command>();
        if (listNodeContext != null) commands.addAll(listNodeContext.createCommands(this, commandContext));
        if (objectNodeContext != null) commands.addAll(objectNodeContext.createCommands(this, commandContext));
        if (commandContext != CommandContext.SEARCH) commands.add(new RefreshCommand());

        return commands;
    }

    public String toString()
    {
        //special root handling, default to file name minus suffix
        if (isRoot() && !objectNodeContext.hasToStringMethod())
        {
			File currentFile = appContainer.getGuiContext().getCurrentFile();
			if(currentFile != null)
			{
				String filename = currentFile.getName();
				return filename.substring(0, filename.lastIndexOf("."));
			}
			else
			{
				return objectNodeContext.getClassModel().getContainedClass().getSimpleName();
			}
		}
        if (wrappedObject() != null)
        {
            if(objectNodeContext.hasToStringMethod()) {
                String strValue = wrappedObject().toString();
                if (strValue != null && strValue.length() > 50)
                {
                    strValue = strValue.substring(0, 50);
                }
                return strValue;
            } else {
                Property property = objectNodeContext.getProperty();
                return wrappedObject().getClass().getSimpleName() + (property != null ? ":" + property.getName() : "");
            }
        }
        return listNodeContext.getContainerProperty().getName();
    }

    public boolean containsReferences()
    {
        return listNodeContext != null && listNodeContext.getContainerProperty().containsReferences();
    }

    public boolean isReference()
    {
        return !isRoot() && getParent().containsReferences();  //todo get parent can return true if parent type is a "Container" for another list property which "ContainsReferences"
    }

    public boolean canEdit()
    {
        return objectNodeContext != null && objectNodeContext.canEdit();
    }

    @Override
    public int index() {
        return getParent().indexOf(this);
    }
}