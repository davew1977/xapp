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

import net.sf.xapp.application.api.Application.DefaultAction;
import net.sf.xapp.application.core.Clipboard;
import net.sf.xapp.application.core.MyTreeCellRenderer;
import net.sf.xapp.application.core.NodeBuilder;
import net.sf.xapp.application.editor.Editor;
import net.sf.xapp.application.editor.EditorListener;
import net.sf.xapp.application.utils.tipoftheday.Tip;
import net.sf.xapp.objectmodelling.api.ClassDatabase;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * This is the application's handle to its container. The application container consists of a frame with two
 * main areas. On the left there is a JTree encapsulating the application's data model. On the right there
 * is a bigger pane which the application can populate with anything it likes. The application container
 * provides a convenient generalisation of a GUI for any data model - although it imposes tight restrictions
 * on how the User interaction works. The advantage being that applications can be made quickly and sharing
 * similar user interactions means that new applications look familiar to users and are easy to use.
 *
 * The saving and loading of application data is also generalised using the {@link net.sf.xapp.marshalling.Unmarshaller}
 * and {@link net.sf.xapp.marshalling.Marshaller}
 *
 * Apart from the right hand pane, the specific application can customise the icons for their data model in
 * the JTree on the left, and can access the Menu and Tool bars. They can also provide any number of commands
 * available to the user for the current selected tree node, or set of tree nodes.
 */
public interface ApplicationContainer<T>
{
    public static final ImageIcon FOLDER_ICON = new ImageIcon(ApplicationContainer.class.getResource("/folder.gif"), "list node icon");
    public static final ImageIcon ASTERISK_ICON = new ImageIcon(ApplicationContainer.class.getResource("/asterisk.jpg"), "reference node icon");
    public static final ImageIcon OBJECT_ICON = new ImageIcon(ApplicationContainer.class.getResource("/object.GIF"), "object node icon");


    JFrame getMainFrame();
    
    /**
     * Get menubar for the application.
     * 
     * @return the menubar.
     */
    JMenuBar getMenuBar();
    
    
    /**
     * Get the tool bar for the application.
     * Initially the tool bar is empty, i.e., no actions are installed.
     * Actions can be installed using add() on the toolbar.
     * 
     * @return the toolbar.
     */
    JToolBar getToolBar();
    void setToolBar(JToolBar toolBar);
    
    
    JScrollPane setUserPanel(JComponent panel);
    JScrollPane setUserPanel(JComponent panel, boolean needsScrollPane);
    JComponent getUserPanel();

    void setDividerLocation(int pixels);
    int getDividerLocation();
    Editor getEditor(Object instance, EditorListener listener);

    /**
     * sets a status message in status bar at the bottom of the window
     * @param message the message - should be a single line of text
     */
    void setStatusMessage(String message);

    /**
     * sets a status message in the status bar at the bottom of the window
     * @param message the message - should be a single line of text
     * @param background the color to paint the status bar background, transparent if null
     * @param delay the delay in milliseconds before the message is removed, will not be removed if 0
     */
    void setStatusMessage(String message, Color background, int delay);

    ToolTipHandler getToolTipHandler();
    Node getNode(Object obj);

    /**
     * find all the nodes, if any, that are in fact references to the given object
     * @param obj
     * @return
     */
    List<Node> findReferencingNodes(Object obj);
    void saveToClipboard(Object obj);
    void refreshNode(Node node);
    void refreshNode(Object object);

    /**
     * Add an already created object to the tree
     * @param node the parent node
     * @param obj  the object about to be added
     */
    void add(Node node, Object obj);

    /**
     * Add an already created object to the tree
     * @param node the parent node
     * @param obj  the object about to be added
     * @param index
     */
    void add(Node node, Object obj, int index);
    void addObjectAfter(Object existing, Object newObject);

    /**
     * Pop up an edit window for the given object
     * @param obj
     */
    void edit(Object obj);
    void multiEdit(List<? extends Object> objs);

    /**
     * Expand tree for given user object
     * @param obj
     */
    void expand(Object obj);
    void expand(Node node);
    void collapseAll();

    void setSystemExitOnClose(boolean b);
    boolean confirmAndSave();
    void save();
    void quit();

    T disposeAndReload();

    /**
     * saves the current instance and reloads it. Used for refreshing the entrire application, if for example,
     * there is a djw-include in the master file that may have been externally refreshed
     * @return
     */
    T saveAndReload();

    public GUIContext<T> getGuiContext();

    JTree getMainTree();

    String getExpandedNodesString();

    void setExpandedNodes(String str);

    void setSelectedNode(Object userObject);

    void setSelectedNode(Node node);

    Node getSelectedNode();

    void setUserGUI(Application application);


    Application getUserGUI();

    /**
     * Get a default action.
     * Default actions are present in menus and enabled by default, so using
     * this actions can be disabled.
     *
     * @param action the action to return
     * @return default action.
     */
    AbstractAction getAction(DefaultAction action);

    /**
     * Add a hook to run before this default action.
     *
     * @param action action to which we want to add a before hook.
     * @param hook the actual hook.
     */
    void addBeforeHook(DefaultAction action, Hook hook);

    /**
     * Add a hook to run after this default action.
     *
     * @param action the action to which we want to add an after hook.
     * @param hook the actual hook.
     */
    void addAfterHook(DefaultAction action, Hook hook);

    /**
     * return the coord of the last time the user right clicked on the tree
     * @return
     */
    Point getLatestPopUpPoint();

    JPanel getMainPanel();

    Application getApplication();

    NodeBuilder getNodeBuilder();

    Clipboard getClipboard();

    void removeNode(Node node);

    MyTreeCellRenderer getTreeCellRenderer();

    /**
     * convenience method, short for getGUIContext().getClassDatabase()
     * @return
     */
    ClassDatabase<T> getClassDatabase();

    /**
     * replaces the
     * @param treeNodeFilter
     */
    void setTreeNodeFilter(TreeNodeFilter treeNodeFilter);

    /**
     * shows a random tip from the list in the {@link net.sf.xapp.application.utils.tipoftheday.TipOfDayDialog}
     * @param tips
     */
    void showTipOfTheDay(List<Tip> tips);

    void showPopupAt(JPopupMenu popup, Node node);

    List<Node> getSelectedNodes();

    NodeUpdateApi getNodeUpdateApi();

    /**
     * Interface for application specific hooks.
     * 
     * @author consa
     *
     * TODO Should we perhaps provide the ActionEvent as an argument? 
     */
    interface Hook {
    	public void execute();
    }
}
