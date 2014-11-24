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
import net.sf.xapp.application.api.Application.DefaultAction;
import net.sf.xapp.application.commands.*;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.application.search.SearchContext;
import net.sf.xapp.application.search.SearchFormControl;
import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.application.strategies.StandaloneSaveStrategy;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.tipoftheday.Tip;
import net.sf.xapp.application.utils.tipoftheday.TipOfDayDialog;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.utils.ClassUtils;
import net.sf.xapp.utils.XappException;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO Should there perhaps be an adaptor class providing a default default implementation?
 */
public class ApplicationContainerImpl<T> implements ApplicationContainer<T>, SearchContext
{
    private JTree m_mainTree;
    private JPanel m_mainPanel;
    private JComponent m_userPanel;
    private JComponent bottomLeftPanel;
    private JComponent leftPane;
    private JFrame m_mainFrame;
    private GUIContext<T> m_guiContext;
    private Application m_application;
    private JSplitPane horizSplitPane;
    private JSplitPane leftVertSplitPane;
    private JLabel m_statusBar;
    private NodeBuilder m_nodeBuilder;
    private Context m_context;
    private JMenuBar m_mainMenuBar;
    private ToolTipHandler m_tooltipHandler;
    private SpecialTreeGraphics m_specialTreeGraphics;
    private MyTreeCellRenderer m_treeCellRenderer;
    private List<Command> m_currentKeyCommands = new ArrayList<Command>();
    private boolean m_systemExit = true;
    private SearchFormControl m_searchFormControl;
    private ScheduledThreadPoolExecutor m_executor = new ScheduledThreadPoolExecutor(1);
    private NodeUpdateApi nodeUpdateApi = new StandaloneNodeUpdate(this);

    /**
     * The application tool bar, initially empty.
     * A specific application can add often used actions to make them easily
     * accessible.
     */
    private JToolBar toolbar = new JToolBar();

    /**
     * The default actions for the application.
     */
    private Map<DefaultAction, AbstractAction> actions =
            new HashMap<DefaultAction, AbstractAction>();

    /**
     * Possible before hooks for the default actions, i.e., code that the
     * specific application wants to be executed before running the actual
     * action.
     */
    private Map<DefaultAction, List<Hook>> beforeHooks =
            new HashMap<DefaultAction, List<Hook>>();

    /**
     * Possible after hooks for the default actions, i.e., code that the
     * specific application wants to be executed after running the actual
     * action.
     */
    private Map<DefaultAction, List<Hook>> afterHooks =
            new HashMap<DefaultAction, List<Hook>>();
    private JScrollPane m_mainTreeScrollPane;
    private Point m_latestPopUpPoint = new Point(0, 0);
    private TreeNodeFilter m_treeNodeFilter = new DefaultTreeNodeFilter();
    private SaveStrategy saveStrategy = new StandaloneSaveStrategy(this);


    public ApplicationContainerImpl(GUIContext guiContext)
    {
        m_guiContext = guiContext;
        m_guiContext.init(this);
        m_nodeBuilder = new NodeBuilder(this);
        m_context = new Context();
        m_searchFormControl = new SearchFormControl(guiContext.getClassDatabase(), this);

        // Initialise default actions
        actions.put(DefaultAction.NEW, new NewAction());
        actions.put(DefaultAction.OPEN, new OpenAction());
        actions.put(DefaultAction.QUIT, new ExitAction());
        actions.put(DefaultAction.SAVE, new SaveAction());
        actions.put(DefaultAction.FIND, new FindAction());

        //getMainFrame();

    }

    public void add(JComponent comp, String location) {
       bottomLeftPanel = comp;
    }

    public void setNodeUpdateApi(NodeUpdateApi nodeUpdateApi) {
        this.nodeUpdateApi = nodeUpdateApi;
    }

    public void setSaveStrategy(SaveStrategy saveStrategy) {
        this.saveStrategy = saveStrategy;
    }

    public Point getLatestPopUpPoint()
    {
        return m_latestPopUpPoint;
    }

    public void setUserGUI(Application application)
    {
        m_application = application;
        m_application.init(this);
        m_specialTreeGraphics = application.createSpecialTreeGraphics();
        if (m_specialTreeGraphics != null)
        {
            if (m_specialTreeGraphics != m_application) {
                m_specialTreeGraphics.init(this);
            }
            m_treeCellRenderer.setTreeGraphics(m_specialTreeGraphics);
        }
    }

    public Application getUserGUI()
    {
        return m_application;
    }

    public JFrame getMainFrame()
    {
        if (m_mainFrame == null)
        {
            m_mainFrame = new JFrame();
            m_mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            m_mainFrame.setSize(900, 700);
            m_mainFrame.setContentPane(getMainPanel());
            m_mainFrame.pack();
            m_mainFrame.setJMenuBar(getMenuBar());
            m_mainFrame.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    new ExitAction(false).actionPerformed(null);
                }
            });
            if (SwingUtils.DEFAULT_FRAME_ICON != null)
            {
                m_mainFrame.setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
            }
            updateFrameTitle();
        }

        return m_mainFrame;
    }


    public JScrollPane setUserPanel(JComponent component, boolean needsScrollPane)
    {
        int dividerLocation = getSplitPane().getDividerLocation();
        JScrollPane comp = new JScrollPane(component);
        getSplitPane().setRightComponent(needsScrollPane ? comp : component);
        getSplitPane().setDividerLocation(dividerLocation);
        m_userPanel = component;
        return comp;
    }

    public JScrollPane setUserPanel(JComponent panel)
    {
        return setUserPanel(panel, true);
    }

    public void setDividerLocation(int pixels)
    {
        getSplitPane().setDividerLocation(pixels);
    }

    public int getDividerLocation()
    {
        return getSplitPane().getDividerLocation();
    }

    public Editor getEditor(Object instance, EditorListener listener)
    {
        ClassModel classModel = m_guiContext.getClassDatabase().getClassModel(instance.getClass());
        EditableContext editableContext = new SingleTargetEditableContext(
                classModel.find(instance), SingleTargetEditableContext.Mode.EDIT, getNodeUpdateApi());
        return EditorManager.getInstance().getEditor(editableContext, listener);
    }

    public void setStatusMessage(String message)
    {
        setStatusMessage(message, null, 0);
    }

    public void setStatusMessage(String message, Color background, int delay)
    {
        getStatusBar().setOpaque(background != null);
        getStatusBar().setBackground(background);
        getStatusBar().setText(message);
        if (delay != 0)
        {
            m_executor.schedule(new Runnable()
            {
                public void run()
                {
                    setStatusMessage(" ", null, 0);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }


    public ToolTipHandler getToolTipHandler()
    {
        if (m_tooltipHandler == null)
        {
            m_tooltipHandler = new DefaultToolTipHandler(m_guiContext.getClassDatabase());

        }
        return m_tooltipHandler;
    }

    public Node getNode(Object obj)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getMainTree().getModel().getRoot();
        return find(root, obj);
    }

    public List<Node> findReferencingNodes(Object obj)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getMainTree().getModel().getRoot();
        return findReferencingNodesInternal(root, obj);
    }

    private List<Node> findReferencingNodesInternal(DefaultMutableTreeNode dmtn, Object obj)
    {
        List<Node> results = new ArrayList<Node>();
        Node node = (Node) dmtn.getUserObject();
        if (node.isReference() && node.wrappedObject() == obj)
        {
            results.add(node);
        }
        else
        {
            int childCount = dmtn.getChildCount();
            for (int n = 0; n < childCount; n++)
            {
                DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) dmtn.getChildAt(n);
                results.addAll(findReferencingNodesInternal(childAt, obj));
            }
        }
        return results;
    }

    private Node find(DefaultMutableTreeNode dmtn, Object obj)
    {
        Node node = (Node) dmtn.getUserObject();
        if (!node.isReference() && node.wrappedObject() == obj) return node;
        int childCount = dmtn.getChildCount();
        for (int n = 0; n < childCount; n++)
        {
            DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) dmtn.getChildAt(n);
            Node result = find(childAt, obj);
            if (result != null) return result;
        }
        return null;
    }

    public void saveToClipboard(Object obj)
    {
        ObjectMeta objMeta = getClassDatabase().findOrCreateObjMeta(null, obj);
        getClipboard().setAction(Clipboard.Action.COPY);
        getClipboard().addClipboardObject(objMeta);
    }

    public Node refreshNode(Node node)
    {
        Node newNode = getNodeBuilder().refresh(node);
        m_mainTree.repaint();
        return newNode;
    }

    @Override
    public void refreshNode(Object object)
    {
        refreshNode(getNode(object));
    }

    private JLabel getStatusBar()
    {
        if (m_statusBar == null)
        {
            m_statusBar = new JLabel("ready");
            m_statusBar.setFont(SwingUtils.DEFAULT_FONT);
        }
        return m_statusBar;
    }

    public JComponent getUserPanel()
    {
        if (m_userPanel == null)
        {
            m_userPanel = new JPanel();

        }
        return m_userPanel;
    }

    public void updateFrameTitle()
    {
        String fileName = m_guiContext.getCurrentFile() != null ? m_guiContext.getCurrentFile().getName() : "";
        getMainFrame().setTitle(m_guiContext.getRootType().toString() + " : " + fileName);
    }


    public AbstractAction getAction(DefaultAction action)
    {
        return actions.get(action);
    }

    public JToolBar getToolBar()
    {
        return toolbar;
    }

    @Override
    public void setToolBar(JToolBar toolBar) {
        m_mainPanel.remove(this.toolbar);
        this.toolbar = toolBar;
        m_mainPanel.add(this.toolbar, BorderLayout.NORTH);
    }

    public JMenuBar getMenuBar()
    {
        if (m_mainMenuBar == null)
        {
            m_mainMenuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            JMenuItem saveMenuItem = new JMenuItem();
            saveMenuItem.setAction(getAction(DefaultAction.SAVE));
            JMenuItem openMenuItem = new JMenuItem();
            openMenuItem.setAction(getAction(DefaultAction.OPEN));
            JMenuItem newMenuItem = new JMenuItem();
            newMenuItem.setAction(getAction(DefaultAction.NEW));
            JMenuItem exitMenuItem = new JMenuItem();
            exitMenuItem.setAction(getAction(DefaultAction.QUIT));
            JMenuItem findMenuItem = new JMenuItem();
            findMenuItem.setAction(getAction(DefaultAction.FIND));

            menu.add(newMenuItem);
            menu.add(saveMenuItem);
            menu.add(openMenuItem);
            menu.add(findMenuItem);
            menu.add(exitMenuItem);
            SwingUtils.setFont(menu);
            m_mainMenuBar.add(menu);
        }
        return m_mainMenuBar;
    }

    public GUIContext getGuiContext()
    {
        return m_guiContext;
    }

    public JPanel getMainPanel()
    {
        if (m_mainPanel == null)
        {
            m_mainPanel = new JPanel(new BorderLayout());
            m_mainPanel.add(getSplitPane(), BorderLayout.CENTER);
            m_mainPanel.setPreferredSize(new Dimension(1000, 600));
            m_mainPanel.add(getStatusBar(), BorderLayout.SOUTH);
            m_mainPanel.add(toolbar, BorderLayout.NORTH);
        }
        return m_mainPanel;
    }

    private JSplitPane getSplitPane()
    {
        if (horizSplitPane == null)
        {

            horizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLeftPane(), getUserPanel());

        }
        return horizSplitPane;
    }

    private Component getLeftPane() {
        if(leftPane==null) {
            if(bottomLeftPanel!=null) {
                leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getMainTreeScrollPane(), bottomLeftPanel);
                ((JSplitPane)leftPane).setDividerLocation(350);
                ((JSplitPane)leftPane).setOneTouchExpandable(true);
            } else {
                leftPane = getMainTreeScrollPane();
            }
        }
        return leftPane;
    }

    private JComponent getMainTreeScrollPane() {
        if(m_mainTreeScrollPane ==null ) {
            m_mainTreeScrollPane = new JScrollPane(getMainTree());
            m_mainTreeScrollPane.setPreferredSize(new Dimension(200, 500));
        }
        return m_mainTreeScrollPane;
    }

    public JTree getMainTree()
    {
        if (m_mainTree == null)
        {
            m_mainTree = new JTree();
            m_nodeBuilder.createTree();
            m_mainTree.addMouseListener(new MainTreeMouseListener());
            m_treeCellRenderer = new MyTreeCellRenderer(getToolTipHandler(), m_mainTree);
            m_mainTree.setCellRenderer(m_treeCellRenderer);
            m_mainTree.setFont(SwingUtils.DEFAULT_FONT);
            m_mainTree.addTreeSelectionListener(new MyTreeSelectionListener());
            //activate control-s for save
            KeyStroke ks = KeyStroke.getKeyStroke("control S");
            m_mainTree.getInputMap().put(ks, ks);
            m_mainTree.getActionMap().put(ks, getAction(DefaultAction.SAVE));
            ks = KeyStroke.getKeyStroke("control Q");
            m_mainTree.getInputMap().put(ks, ks);
            m_mainTree.getActionMap().put(ks, getAction(DefaultAction.QUIT));
            ks = KeyStroke.getKeyStroke("control F");
            m_mainTree.getInputMap().put(ks, ks);
            m_mainTree.getActionMap().put(ks, getAction(DefaultAction.FIND));
            ToolTipManager.sharedInstance().registerComponent(m_mainTree);
        }
        return m_mainTree;
    }

    private List<Node> getNodes(TreePath[] paths)
    {
        List<Node> nodes = new ArrayList<Node>();
        if (paths != null)
        {
            for (TreePath path : paths)
            {
                nodes.add(getNode(path));
            }
        }
        return nodes;
    }

    private Node getNode(TreePath selectionPath)
    {
        if (selectionPath == null) return null;
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        return (Node) treeNode.getUserObject();
    }

    public Node getSelectedNode()
    {
        return getNode(getMainTree().getSelectionPath());
    }

    public List<Node> getSelectedNodes() {
        return getNodes(m_mainTree.getSelectionPaths());
    }

    @Override
    public NodeUpdateApi getNodeUpdateApi() {
        return nodeUpdateApi;
    }

    @Override
    public Node createNode(ObjectLocation parent, ObjectMeta objectMeta) {
        Node node = getNodeBuilder().getNode(parent.getObj().getId());
        if (node != null) {
            node = node.find(parent.getProperty());
        }
        return createNode(node, objectMeta);
    }

    public Node createNode(Node parent, ObjectMeta objectMeta) {
        Node newNode = getNodeBuilder().createNode(parent, objectMeta);
        getApplication().nodeAdded(newNode);
        getMainPanel().repaint();
        return newNode;
    }

    @Override
    public Node getNode(Long id, ObjectLocation objectLocation) {
        return getNodeBuilder().getNode(id, objectLocation);
    }

    @Override
    public Collection<Node> getRefNodes(Long id) {
        return getNodeBuilder().getRefNodes(id);
    }

    private List<Command> getCommands(Node node, CommandContext commandContext)
    {
        List<Command> commands = new ArrayList<Command>();
        commands.addAll(node.createCommands(commandContext));
        //add user commands too!
        if (node.wrappedObject() != null)
        {
            commands.addAll(m_application.getCommands(node));
        }
        return commands;
    }

    private JPopupMenu createPopUp(List<Command> commands, final Object arg)
    {
        JPopupMenu menu = new JPopupMenu();
        for (final Command command : commands)
        {
            JMenuItem menuItem = new JMenuItem(command.getName());
            menuItem.setFont(SwingUtils.DEFAULT_FONT);
            menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        command.execute(arg);
                        getMainFrame().repaint();
                    }
                    catch (RuntimeException ex)
                    {
                        m_application.handleUncaughtException(ex);
                    }
                }
            });
            menuItem.setToolTipText(command.getDescription());
            menuItem.setAccelerator(KeyStroke.getKeyStroke(command.getKeyStroke()));
            menu.add(menuItem);
        }
        return menu;
    }

    public void add(Node parentNode, Object obj)
    {
        add(parentNode, obj, -1);
    }

    public void add(Node parentNode, Object obj, int index)
    {
        Node node = getNode(obj);
        if (node != null) {
            throw new XappException("object " + obj + " is already added");
        }
        getNodeUpdateApi().insertObject(parentNode.toObjLocation(), obj);
    }

    @Override
    public void addObjectAfter(Object existing, Object newObject)
    {
        Node node = getNode(existing);
        Node parent = node.getParent();
        int index = parent.getListNodeContext().getList().indexOf(existing);
        add(parent, newObject, index + 1);
        refreshNode(parent);

    }

    public void edit(Object obj)
    {
        Node node = getNode(obj);
        if (node == null) throw new XappException("object " + obj + " is not managed");
        new EditCommand().execute(node);
    }

    @Override
    public void multiEdit(List<? extends Object> objs)
    {
        List<Node> nodes = getNodes((List)objs);
        Class commonClass = getCommonClass(nodes);
        ClassModel commonClassModel = this.getGuiContext().getClassDatabase().getClassModel(commonClass);

        new MultiEditCommand(this, nodes, commonClassModel).execute(null);
    }

    public void expand(Object obj)
    {
        Node node = getNode(obj);
        if (node == null) throw new XappException("object " + obj + " is not managed");
        expand(node);
    }

    public void expand(final Node node)
    {
        getMainTree().setSelectionPath(node.getPath());
        Rectangle pathBounds = getMainTree().getPathBounds(node.getPath());
        if (pathBounds!=null)
        {
            m_mainTree.scrollRectToVisible(pathBounds);
        }
    }

    public void collapseAll()
    {
        List<TreePath> paths = new ArrayList<TreePath>();
        for (int i = 0; i < getMainTree().getRowCount(); i++)
        {
            paths.add(getMainTree().getPathForRow(i));
        }
        Collections.sort(paths, new Comparator<TreePath>()
        {
            public int compare(TreePath o1, TreePath o2)
            {
                Integer i1 = o1.getPathCount();
                Integer i2 = o2.getPathCount();
                return i2.compareTo(i1);
            }
        });
        for (TreePath path : paths)
        {
            getMainTree().collapsePath(path);
        }
    }

    public void setSystemExitOnClose(boolean b)
    {
        m_systemExit = b;
    }

    public boolean confirmAndSave()
    {
        int option = JOptionPane.showOptionDialog(m_mainFrame, "Save?", "Save " + m_guiContext.getCurrentFile().getName(), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        if (option == JOptionPane.NO_OPTION) return false;
        save();
        return true;
    }

    public void save()
    {
        getAction(DefaultAction.SAVE).actionPerformed(null);
    }

    public void quit()
    {
        getAction(DefaultAction.QUIT).actionPerformed(null);
    }

    public NodeBuilder getNodeBuilder()
    {
        return m_nodeBuilder;
    }

    public Clipboard getClipboard()
    {
        return m_context.getClipboard();
    }

    @Override
    public void removeNode(Long objId) {
        removeNode(getNodeBuilder().getNode(objId));
    }

    public Application getApplication()
    {
        return m_application;
    }

    public String getExpandedNodesString()
    {
        TreePath rootPath = new TreePath(((DefaultMutableTreeNode) m_mainTree.getModel().getRoot()).getPath());
        String str = "";
        Enumeration<TreePath> expandedNodes = m_mainTree.getExpandedDescendants(rootPath);
        if (expandedNodes == null) return "";
        while (expandedNodes.hasMoreElements())
        {
            TreePath treePath = expandedNodes.nextElement();
            Object[] nodes = treePath.getPath();
            String path = "";
            for (int i = 0; i < nodes.length - 1; i++)
            {
                TreeNode treeNode = (TreeNode) nodes[i];
                int nextNodeIndex = treeNode.getIndex((TreeNode) nodes[i + 1]);
                path += nextNodeIndex + ",";
            }
            str += path + ":";
        }
        return str;
    }

    public void setExpandedNodes(String str)
    {
        String[] pathsStrs = str.split(":");
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) m_mainTree.getModel().getRoot();
        List<TreePath> treePathList = new ArrayList<TreePath>();
        for (String pathsStr : pathsStrs)
        {
            List<Object> nodeList = new ArrayList<Object>();
            TreeNode currentNode = rootNode;
            nodeList.add(rootNode);
            String[] nodeIndexes = pathsStr.split(",");
            for (String nodeIndexe : nodeIndexes)
            {
                if (nodeIndexe.equals("")) continue;
                int nodeIndex = Integer.parseInt(nodeIndexe);
                if (nodeIndex >= currentNode.getChildCount()) continue;
                TreeNode child = currentNode.getChildAt(nodeIndex);
                nodeList.add(child);
                currentNode = child;
            }

            TreePath treePath = new TreePath(nodeList.toArray());
            treePathList.add(treePath);
        }
        TreePath[] paths = treePathList.toArray(new TreePath[0]);
        m_mainTree.setSelectionPaths(paths);
    }

    public void setSelectedNode(Object userObject)
    {
        setSelectedNode(getNode(userObject));
    }

    public void setSelectedNode(Node node)
    {
        if (node.getParent()!=null) //parent can be null if the node has been filtered out
        {
            getMainTree().clearSelection();//force a change in the tree listener
            getMainTree().setSelectionPath(node.getPath());
            getMainTree().scrollPathToVisible(node.getPath());
        }
    }


    /**
     * Run a hook associated with a specific action.
     *
     * @param action the action we are executing
     * @param hookMap  the collection of hooks.  This will be either
     *               {@link #beforeHooks} or {@link #afterHooks}.
     */
    private void runHook(DefaultAction action, Map<DefaultAction, List<Hook>> hookMap)
    {
        List<Hook> hooks = hookMap.get(action);
        if(hooks != null) {
            for (Hook hook : hooks) {
                hook.execute();
            }
        }
    }

    private void runAfterHook(DefaultAction action)
    {
        runHook(action, afterHooks);
    }

    private void runBeforeHook(DefaultAction action)
    {
        runHook(action, beforeHooks);
    }


    /**
     * Add a before or after hook to a specific action.
     *
     * @param hookMap  the collection of hooks.  This will be either {@link #beforeHooks}
     *               or {@link #afterHooks}.
     * @param action the action we want to add a hook to.
     * @param hook   the actual hook.
     */
    private void addHook(Map<DefaultAction, List<Hook>> hookMap, DefaultAction action, Hook hook)
    {
        List<Hook> hooks = hookMap.get(action);
        if(hooks == null) {
            hooks = new ArrayList<Hook>();
            hookMap.put(action, hooks);
        }
        hooks.add(hook);
    }

    public void addAfterHook(DefaultAction action, Hook hook)
    {
        addHook(afterHooks, action, hook);
    }

    public void addBeforeHook(DefaultAction action, Hook hook)
    {
        addHook(beforeHooks, action, hook);
    }

    public void removeNode(Node node) {

        removeNode(node, false);
    }
    public void removeNode(Node node, boolean wasCut) {
        removeNode(node, true, wasCut);
    }
    public void removeNode(Node node, boolean notifyApp, boolean wasCut)
    {
        //note this method only modifies the JTree NOT the data model
        if (node != null) {
            if (notifyApp) {
                getApplication().nodeAboutToBeRemoved(node, wasCut);
            }
            ((DefaultTreeModel) getMainTree().getModel()).removeNodeFromParent(node.getJtreeNode());
            getNodeBuilder().nodeRemoved(node);
        }
    }

    public void searchResultSelected(Object selectedValue)
    {
        Node node = getNode(selectedValue);
        setSelectedNode(node);
    }

    public List<Node> getNodes(List<Object> selectedValues)
    {
        List<Node> nodes = new ArrayList<Node>();
        for (Object selectedValue : selectedValues)
        {
            nodes.add(getNode(selectedValue));
        }
        return nodes;
    }

    public List<Command> getCommands(List<Node> nodes)
    {
        if (nodes.size() == 1)
        {
            return getCommands(nodes.get(0), CommandContext.SEARCH);
        }
        return getMultiCommands(nodes);
    }

    public Icon getIcon(Object obj)
    {
        return m_treeCellRenderer.getIcon(new FakeNode(obj));
    }

    public void update()
    {
        getMainFrame().repaint();
    }

    public TreeNodeFilter getNodeFilter()
    {
        return m_treeNodeFilter;
    }


    /**
     * A, as of now, tentative stab at implementing actions with hooks.
     * The idea is that we can provide a default action and then augment it
     * with hooks, i.e., stuff to be done before and/or after the actual
     * default action.
     * <p/>
     * Actual default actions should subclass this class and override
     * the method action() and in that way provide the default action.
     * <p/>
     * Hooks are attached directly on the action using public method.  This is
     * better than storing them in a Map, since we then keep this local with
     * the action.
     * <p/>
     * TODO Define when the actions should be run, especially with regard to
     * any user interaction.  Currently, there is an inconsistency for the
     * before hooks for {@link ExitAction} and {@link SaveAction}.
     *
     * @author consa
     */
    class HookedAction extends AbstractAction
    {
        private Hook before;
        private Hook after;

        public HookedAction(DefaultAction action, String name)
        {
            super(name);
        }

        public void addBeforeHook(Hook hook)
        {
            before = hook;
        }

        public void addAfterHook(Hook hook)
        {
            after = hook;
        }

        private void runHook(Hook hook)
        {
            if (hook != null)
            {
                hook.execute();
            }
        }

        public void actionPerformed(ActionEvent e)
        {
            runHook(before);
            action(e);
            runHook(after);
        }

        protected void action(ActionEvent e)
        {
        }
    }

    /*
       INNER CLASSES...
    */

    /**
     * Default action when exiting the application.
     * The user is asked whether a save should be performed before exit.  If so,
     * the default save action is run.
     *
     * @author consa
     *         <p/>
     *         TODO Abstract this into two separate actions to preserve an ideal of
     *         having only one instance for each action.  While it is reasonable to
     *         have an exit action that one can cancel, we should probably provide
     *         this as a separate action.  These two actions should possible be chained.
     */
    private class ExitAction extends AbstractAction
    {
        private DefaultAction myaction = DefaultAction.QUIT;
        boolean m_allowCancel;

        public ExitAction()
        {
            this(true);
        }

        /**
         * Exit the application.
         *
         * @param allowCancel true if we are allowed to cancel the exit action.
         */
        public ExitAction(boolean allowCancel)
        {
            super("Quit");
            m_allowCancel = allowCancel;
        }

        public void actionPerformed(ActionEvent e)
        {
            int optionType = m_allowCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION;
            int option = JOptionPane.showOptionDialog(m_mainFrame, "Save before close?", "Exit", optionType, JOptionPane.INFORMATION_MESSAGE, null, null, null);

            if (option == JOptionPane.CANCEL_OPTION) return;
            if (option == JOptionPane.YES_OPTION)
            {
                getAction(DefaultAction.SAVE).actionPerformed(null);
            }

            runBeforeHook(myaction);
            getMainFrame().dispose();
            if (m_systemExit) System.exit(0);
            runAfterHook(myaction);
        }
    }


    private class SaveAction extends AbstractAction
    {
        private DefaultAction myAction = DefaultAction.SAVE;

        public SaveAction()
        {
            super("Save");
        }

        public void actionPerformed(ActionEvent e)
        {
            runBeforeHook(myAction);
            saveStrategy.save();

            runAfterHook(myAction);
        }
    }

    private class FindAction extends AbstractAction
    {
        public FindAction()
        {
            super("Find");
        }

        public void actionPerformed(ActionEvent e)
        {
            TreePath path = getMainTree().getSelectionPath();
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) (path != null ? path.getLastPathComponent() : getMainTree().getModel().getRoot());
            Node node = (Node) dmtn.getUserObject();
            Object wrappedObj = node.wrappedObject();
            if (wrappedObj != null)
            {
                m_searchFormControl.setSearchObject(wrappedObj);
                m_searchFormControl.showFrame();
            }
        }
    }

    public T saveAndReload()
    {
        save();
        return disposeAndReload();
    }



    public T disposeAndReload()
    {
        File currentFile = m_guiContext.getCurrentFile();
        T o = m_guiContext.openFile(currentFile);
        resetUI_internal();
        return o;
    }

    public void resetUI(ObjectMeta objMeta) {
        getGuiContext().setObjMeta(objMeta);
        resetUI_internal();
    }

    private void resetUI_internal() {
        m_nodeBuilder.createTree();
        updateFrameTitle();
        EditorManager.getInstance().reset(); //editor widgets can contain stale references
    }

    private class OpenAction extends AbstractAction
    {
        private DefaultAction myAction = DefaultAction.OPEN;

        public OpenAction()
        {
            super("Open");
        }

        public void actionPerformed(ActionEvent e)
        {
            runBeforeHook(myAction);

            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            int returnVal = chooser.showOpenDialog(getMainPanel());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                m_guiContext.openFile(chooser.getSelectedFile());
            }
            m_nodeBuilder.createTree();
            updateFrameTitle();

            runAfterHook(myAction);
        }
    }


    private class NewAction extends AbstractAction
    {
        private DefaultAction myAction = DefaultAction.NEW;

        public NewAction()
        {
            super("New");
        }

        public void actionPerformed(ActionEvent e)
        {
            runBeforeHook(myAction);

            m_guiContext.newObjectInstance();
            m_nodeBuilder.createTree();
            updateFrameTitle();

            runAfterHook(myAction);
        }
    }


    private class MainTreeMouseListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
            Node selectedNode = getSelectedNode();
            if (e.getClickCount() == 2 && !e.isPopupTrigger() && selectedNode != null)
            {
                if (selectedNode.isReference())
                {
                    new ExpandReferenceCommand().execute(selectedNode);
                }
                else
                {
                    m_application.nodeDoubleClicked(selectedNode);
                }
                /*else if (selectedNode.canEdit())
                {
                    new EditCommand().execute(selectedNode);
                }*/
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                TreePath[] selectedPaths = getMainTree().getSelectionPaths();

                List<TreePath> selectedPathList = selectedPaths != null ? Arrays.asList(selectedPaths) : null;
                TreePath pathUnderPointer = getMainTree().getPathForLocation(e.getX(), e.getY());
                if (pathUnderPointer == null) return;
                JPopupMenu popup;
                if (selectedPathList != null && selectedPathList.size() > 1 && selectedPathList.contains(pathUnderPointer))
                {
                    List<Node> nodes = getNodes(selectedPaths);
                    List<Command> commands = getMultiCommands(nodes);
                    popup = createPopUp(commands, nodes);
                }
                else
                {
                    Node node = getNode(pathUnderPointer);
                    popup = createPopUp(getCommands(node, CommandContext.POP_UP), node);
                }
                popup.show(e.getComponent(), e.getX() + 10, e.getY() + 10);
                m_latestPopUpPoint = new Point(e.getX(), e.getY());
            }
        }

    }

    private class MyTreeSelectionListener implements TreeSelectionListener
    {
        public void valueChanged(TreeSelectionEvent e)
        {
            //remove current key commands
            for (Command command : m_currentKeyCommands)
            {
                removeKeyStroke(command);
            }
            m_currentKeyCommands.clear();
            //is it single/multi selection?
            TreePath[] treePaths = m_mainTree.getSelectionPaths();
            Object commandArg = null;
            List<Node> nodes = getNodes(treePaths);
            if (nodes.size() == 1)
            {
                Node node = getNode(m_mainTree.getSelectionPath());
                commandArg = node;
                m_application.nodeSelected(node);
                m_currentKeyCommands = getCommands(node, CommandContext.KEY_BINDING);
            }
            else if (nodes.size() > 1)  //multi
            {
                commandArg = nodes;
                m_currentKeyCommands = getMultiCommands(nodes);
            }

            if (!nodes.isEmpty()) {
                Class commonClass = getCommonClass(nodes);
                m_application.nodesSelected(nodes, commonClass == null ? Object.class : commonClass);
            }

            for (Command command : m_currentKeyCommands)
            {
                addKeyStroke(commandArg, command);
            }
        }

        private void addKeyStroke(final Object arg, final Command command)
        {
            InputMap im = m_mainTree.getInputMap();
            ActionMap actionMap = m_mainTree.getActionMap();
            String ks = command.getKeyStroke();
            KeyStroke k = KeyStroke.getKeyStroke(ks);
            if (k != null)
            {
                im.put(k, k);
                actionMap.put(k, new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        try
                        {
                            command.execute(arg);
                            getMainFrame().repaint();
                        }
                        catch (RuntimeException e1)
                        {
                            m_application.handleUncaughtException(e1);
                        }
                    }
                });
            }
            else if (ks != null)
            {
                System.out.println("WARNING - no key stroke matched for " + ks);
            }
        }

        private void removeKeyStroke(Command command)
        {
            InputMap im = m_mainTree.getInputMap();
            ActionMap actionMap = m_mainTree.getActionMap();
            if (command.getKeyStroke() == null) return;
            KeyStroke ks = KeyStroke.getKeyStroke(command.getKeyStroke());
            assert ks != null;
            actionMap.remove(ks);
            im.remove(ks);
        }
    }

    private List<Command> getMultiCommands(List<Node> nodes)
    {
        ArrayList<Command> commands = new ArrayList<Command>();
        commands.add(new RemoveAllCommand());
        //if all nodes have same class then add an MultiEditCommand
        Class commonClass = getCommonClass(nodes);
        if (commonClass != null && !commonClass.equals(Object.class))
        {
            ClassModel commonClassModel = this.getGuiContext().getClassDatabase().getClassModel(commonClass);
            commands.add(new MultiEditCommand(this, nodes, commonClassModel));
            commands.add(new MultiCutCommand(nodes));
        }

        if (allAreSimpleNodes(nodes))
        {
            commands.addAll(m_application.getCommands(nodes, commonClass));
        }
        return commands;
    }

    /**
     * @param nodes
     * @return The "lowest" common class, Object.class if not in same heirarchy
     */
    public static Class getCommonClass(List<Node> nodes)
    {
        ArrayList<Class> classList = new ArrayList<Class>();
        for (Node node : nodes)
        {
            if (node.wrappedObject() != null)
            {
                classList.add(node.wrappedObjectClass());
            }
        }

        return ClassUtils.getCommonClass(classList);
    }

    private boolean allAreSimpleNodes(List<Node> nodes)
    {
        for (Node node : nodes)
        {
            if (node.wrappedObject() == null)
            {
                return false;
            }
        }
        return true;
    }

    public MyTreeCellRenderer getTreeCellRenderer()
    {
        return m_treeCellRenderer;
    }

    public ClassDatabase getClassDatabase()
    {
        return getGuiContext().getClassDatabase();
    }

    public void setTreeNodeFilter(TreeNodeFilter treeNodeFilter)
    {
        m_treeNodeFilter = treeNodeFilter != null ? treeNodeFilter : new DefaultTreeNodeFilter();
        m_nodeBuilder.createTree();
    }

    public void showTipOfTheDay(List<Tip> tips)
    {
        TipOfDayDialog d = new TipOfDayDialog(getMainFrame(), tips);
        d.setLocationRelativeTo(getMainFrame());
        d.setVisible(true);
    }

    @Override
    public void showPopupAt(JPopupMenu popup, Node node)
    {
        Rectangle bounds = m_mainTree.getPathBounds(node.getPath());
        popup.show(m_mainTree, bounds.x, bounds.y);

    }

    private static class DefaultTreeNodeFilter implements TreeNodeFilter
    {
        public boolean accept(ObjectMeta objectMeta)
        {
            return true;
        }
    }

    public static <E> List<E> toObjList(List<Node> nodes){
        List<E> result = new ArrayList<E>();
        for (Node node : nodes)
        {
            result.add(node.<E>wrappedObject());
        }
        return result;
    }
}
