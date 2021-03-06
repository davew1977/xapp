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
import net.sf.xapp.utils.ReflectionUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node {
    protected final ApplicationContainer appContainer;
    protected final DefaultMutableTreeNode jTreeNode;
    private ListNodeContext listNodeContext;
    private ObjectNodeContext objectNodeContext;

    protected NodeImpl(ApplicationContainer appContainer) {
        this.appContainer = appContainer;
        jTreeNode = new DefaultMutableTreeNode();
    }

    public NodeImpl(ApplicationContainer appContainer, Node parent, int insertIndex, ObjectLocation objectLocation) {
        this(appContainer);
        listNodeContext = new ListNodeContext(objectLocation);
        addToJTree(parent, insertIndex);
    }

    public NodeImpl(ApplicationContainer appContainer, Node parent, int insertIndex, ObjectMeta objectMeta, Property locProperty) {
        this(appContainer);
        objectNodeContext = new ObjectNodeContext(this, objectMeta, locProperty);
        if (objectMeta.isContainer()) {
            listNodeContext = new ListNodeContext(new ObjectLocation(objectMeta, objectMeta.getContainerProperty()));
        }
        addToJTree(parent, insertIndex);
        if(!isReference()) {
            objectMeta.attach(this);
        } else {
            objectMeta.attach(myObjLocation(), this);
        }
    }

    private void addToJTree(Node parent, int insertIndex) {

        jTreeNode.setUserObject(this);
        DefaultTreeModel treeModel = (DefaultTreeModel) appContainer.getMainTree().getModel();
        if (parent!=null) {
            if(insertIndex == -1) {
                insertIndex =  parent.numChildren();
            }
            treeModel.insertNodeInto(jTreeNode, parent.getJtreeNode(), insertIndex);
        }
    }

    public DefaultMutableTreeNode getJtreeNode() {
        return jTreeNode;
    }

    public boolean isRoot() {
        return jTreeNode != null && jTreeNode.isRoot();
    }

    public Node getParent()
    {
        DefaultMutableTreeNode parentJTreeNode = (DefaultMutableTreeNode) jTreeNode.getParent();
        //parent jtree node can be null if the node was not added to jtree (it was filtered out)
        return parentJTreeNode != null ? (Node) parentJTreeNode.getUserObject() : null;
    }

    public Node getChildBefore(Node node) {
        DefaultMutableTreeNode childBefore = (DefaultMutableTreeNode) jTreeNode.getChildBefore(node.getJtreeNode());
        return childBefore != null ? (Node) childBefore.getUserObject() : null;
    }

    @Override
    public Node getChildAfter(Node node) {
        DefaultMutableTreeNode childAfter = (DefaultMutableTreeNode) jTreeNode.getChildAfter(node.getJtreeNode());
        return childAfter != null ? (Node) childAfter.getUserObject() : null;
    }

    public Node getChildAt(int index) {
        return (Node) ((DefaultMutableTreeNode) jTreeNode.getChildAt(index)).getUserObject();
    }

    public ObjectNodeContext getObjectNodeContext() {
        return objectNodeContext;
    }

    public ListNodeContext getListNodeContext() {
        return listNodeContext;
    }

    @Override
    public Class wrappedObjectClass() {
        return wrappedObject().getClass();
    }

    @Override
    public <T> boolean isA(Class<T> aClass) {
        return aClass.isInstance(wrappedObject());
    }

    public Object wrappedObject() {
        return objectNodeContext != null ? objectNodeContext.instance() : null;
    }

    public Object nearestWrappedObject() {
        return objectNodeContext != null ? objectNodeContext.instance() : getParent().wrappedObject();
    }

    public Node parentObjectNode() {
        Node parent = getParent();
        return parent == null || parent.getObjectNodeContext() != null ? parent : parent.parentObjectNode();
    }

    public ObjectMeta parentObjectMeta() {
        return parentObjectNode().objectMeta();
    }

    @Override
    public ObjectMeta objectMeta() {
        return objectNodeContext != null ? getObjectNodeContext().objectMeta() : getParent().objectMeta();
    }

    @Override
    public Node refresh() {
        return getAppContainer().refreshNode(this);
    }

    @Override
    public ObjectLocation toObjLocation() {
        //todo would really like to return a location even if we have no list context, but...
        return listNodeContext != null ? new ObjectLocation(listNodeContext.getObjectLocation()) : null;
    }

    @Override
    public ObjectLocation myObjLocation() {
        if(objectNodeContext != null && objectNodeContext.getLocProperty() != null) {
            return new ObjectLocation(getParent().objectMeta(), objectNodeContext.getLocProperty());
        }
        ObjectLocation objectLocation = getParent().toObjLocation();
        if (objectLocation != null) {
            objectLocation.setIndex(index());
        }
        return objectLocation;
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
        if (newIndex > oldIndex) {
            appContainer.getApplication().nodeMovedDown(this);
        } else {
            appContainer.getApplication().nodeMovedUp(this);
        }
    }

    @Override
    public boolean isObjectNode() {
        return objectNodeContext != null;
    }

    @Override
    public Node find(Property property) {
        for(int i=0; i<numChildren(); i++) {
            Node node = getChildAt(i);
            ObjectLocation objectLocation = node.myObjLocation();
            if(objectLocation != null && objectLocation.getProperty().equals(property)) {
                return node;
            }
            if(node.getListNodeContext() != null && node.getListNodeContext().getContainerProperty().equals(property)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Node closest(Class filter) {
        if(isA(filter)) {
            return this;
        } else {
            Node parent = parentObjectNode();
            return parent != null ? parent.closest(filter) : null;
        }
    }

    @Override
    public Node closest(String method, Class... parameterTypes) {
        if(ReflectionUtils.hasMethodInHierarchy(wrappedObjectClass(), method, parameterTypes)){
            return this;
        } else {
            Node parent = parentObjectNode();
            return parent != null ? parent.closest(method) : null;
        }
    }

    @Override
    public Property getLocProperty() {
        return getObjectNodeContext().getLocProperty();
    }


    public ApplicationContainer getAppContainer() {
        return appContainer;
    }

    public int indexOf(Node node) {
        return jTreeNode.getIndex(node.getJtreeNode());
    }

    public int numChildren() {
        return jTreeNode.getChildCount();
    }

    public TreePath getPath() {
        return new TreePath(jTreeNode.getPath());
    }

    public List<Command> createCommands(CommandContext commandContext) {
        ArrayList<Command> commands = new ArrayList<Command>();
        if (listNodeContext != null) {
            commands.addAll(listNodeContext.createCommands(this, commandContext));
        }
        if (objectNodeContext != null) {
            commands.addAll(objectNodeContext.createCommands(commandContext));
        }
        if (commandContext != CommandContext.SEARCH) commands.add(new RefreshCommand());

        return commands;
    }

    public String toString() {
        //special root handling, default to file name minus suffix
        if (isRoot() && isObjectNode() && !objectNodeContext.hasToStringMethod()) {
            File currentFile = appContainer.getGuiContext().getCurrentFile();
            if (currentFile != null) {
                String filename = currentFile.getName();
                return filename.substring(0, filename.lastIndexOf("."));
            } else {
                return objectMeta().getSimpleClassName();
            }
        }
        else if (isObjectNode()) {
            return objectNodeContext.toString();
        }
        return listNodeContext.getContainerProperty().getName();
    }

    public boolean containsReferences() {
        return listNodeContext != null && listNodeContext.getContainerProperty().containsReferences();
    }

    public boolean isReference() {
        return !isRoot() && getParent().containsReferences();  //todo get parent can return true if parent type is a "Container" for another list property which "ContainsReferences"
    }

    public boolean canEdit() {
        return objectNodeContext != null && objectNodeContext.canEdit();
    }

    @Override
    public int index() {
        return getParent().indexOf(this);
    }
}