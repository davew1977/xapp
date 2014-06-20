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
package net.sf.xapp.application.api;

import net.sf.xapp.application.core.CommandContext;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

/**
 * Wrapper class for the Swing {@link javax.swing.tree.TreeNode} class. It encapsulates a node in the
 * application tree. Nodes can have several cross-cutting properties:
 *    1) The node is a simple list containing objects. It is backed by a list property in the data model
 *    2) The node is backed by an object in the data model. Its child nodes represent the object's properties
 *    3) The node is backed by an object in the data model that is annotated as a Container. Its child nodes
 *       are either the object's properties, or they are items in the Container object's primary list.
 *    4) The node's parent is a list (type 1 or 3)
 *    5) The node's parent is an object (type 2)
 *    6) The node is a list (type 1 or 3) but it contains references to other objects (it is annotated with
 *       {@link net.sf.xapp.annotations.objectmodelling.ContainsReferences})
 *
 * These properties affect the way the node is drawn as well as the actions that are available for the node
 */
public interface Node
{
    //TREENODE API: methods that delegate to the underlying JTree node
    boolean isRoot();
    Node getParent();
    Node getChildBefore(Node node);
    Node getChildAfter(Node node);
    Node getChildAt(int index);
    int indexOf(Node node);
    DefaultMutableTreeNode getJtreeNode();
    void setJtreeNode(DefaultMutableTreeNode dmtn);
    int numChildren();
    TreePath getPath();

    /**
     * @return a handle to the entire application
     */
    ApplicationContainer getAppContainer();

    /**
     * @param commandContext shows whether the commands are for execution in a pop up menu, assigned to key combinations or in the search window
     * @return
     */
    List<Command> createCommands(CommandContext commandContext);

    /**
     * @return null if the node is backed by a list property in the data model
     */
    ObjectNodeContext getObjectNodeContext();
    Node parentObjectNode();

    /**
     * @return null if the node is backed by a non-container object
     */
    ListNodeContext getListNodeContext();

    /**
     * @return the data object wrapped by this node, null if a simple list node
     */
    <T> T wrappedObject();

    /**
     * return true if the wrapped object is of type aClass
     * @param aClass
     * @param <T>
     * @return
     */
    <T> boolean isA(Class<T> aClass);
    /**
     *
     * @return as above, but if a list node, this will get the parent's wrapped object
     */
    <T> T nearestWrappedObject();

    /**
     * @return true if this is a list container whose items are references to other objects
     */
    boolean containsReferences();

    /**
     * @return true if the nodes parent containsReferences.
     */
    boolean isReference();

    boolean canEdit();

    int index();

    ObjectMeta objectMeta();

    void refresh();

    /**
     * call when creating an obj location from the current node
     * node must be a container for child nodes (listnode context is assumed to have a value)
     * @return an object location
     */
    ObjectLocation asObjLocation();

    /**
     *
     * @return the obj location of THIS node, assumes objnode context
     */
    ObjectLocation thisObjLocation();

    ClassDatabase getClassDatabase();

    void updateIndex(int newIndex);
}