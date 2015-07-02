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

import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyChange;

import java.util.List;
import java.util.Map;

/**
 * Interface that all djwastor applications must implement. A base implementation (empty) is provided
 * to make it easy to get an application up and running: {@link SimpleApplication}
 */
public interface Application<T>
{
    void handleUncaughtException(Throwable e);

    /**
     * factory method for app to create custom jtree node graphics
     * @return the specialised graphics
     */
    SpecialTreeGraphics createSpecialTreeGraphics();

    void nodeDoubleClicked(Node selectedNode);

    void nodesSelected(List<Node> nodes, Class commonClass);

    void fileReloaded();

    /**
	 * Indicators for default actions built into the framework.
	 * This is set is easy to extend when additional deefault actions
	 * are added such as SAVE_AS, SEARCH, UNDO etc.
	 * 
	 * @author consa
	 *
	 */
	public enum DefaultAction {
		OPEN,
		NEW,
		SAVE,
		QUIT,
        FIND
    }
	
    /**
     * Get the application level commands available for this node
     * @param node the selected node in the application's GUI tree
     * @return the user apps commands it would like in the tree pop up menu
     */
    List<Command> getCommands(Node node);

    /**
     * Get the application level commands available for this set of nodes
     * @param nodes the selected nodes in the application's GUI tree
     * @param commonType
     * @return
     */
    List<Command> getCommands(List<Node> nodes, Class commonType);

    /**
     * Notification that the node was selected in the Application GUI
     * @param node the selected node
     * @return true is a signal to sub classes that it has "consumed" the event
     */
    boolean nodeSelected(Node node);

    /**
     * Notification that a node was removed from the Application GUI
     * @param node
     * @param wasCut node was removed after being pasted somewhere else
     */
    void nodeAboutToBeRemoved(Node node, boolean wasCut);

    /**
     * Notification that a node was added to the Application GUI tree
     * @param node
     */
    void nodeAdded(Node node);

    /**
     * Notification that a node is about to be added to the Application GUI tree. The application cannot
     * veto this, but it can manipulate the data model as it wishes.
     *
     * The notification is made before the Editor for the newChild is presented on the screen
     * @param newChild
     */
    void nodeAboutToBeAdded(ObjectLocation homeLocation, ObjectMeta newChild);

    /**
     * A notification that a datamodel object has been edited. The user hit the save button in an editor
     * @param objectNode
     * @param changes
     */
    void nodeUpdated(Node objectNode, Map<String,PropertyChange> changes);

    /**
     * The node was moved up in its parent's child list
     * @param node
     */
    void nodeMovedUp(Node node);

    /**
     * The node was moved down in its parent's child list
     * @param node
     */
    void nodeMovedDown(Node node);

    /**
     * Called at startup to provide the application with a reference to its container
     * @param applicationContainer
     */
    void init(ApplicationContainer<T> applicationContainer);

}
