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

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.utils.XappException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleApplication<T> implements Application<T> {
    protected ApplicationContainer<T> appContainer;
    protected JComponent userPanel;
    private boolean needsScrollPane;
    private AppData appData;
    //todo add list of plugins

    public JScrollPane setUserPanel(JComponent panel) {
        return setUserPanel(panel, true);
    }

    public JScrollPane setUserPanel(JComponent panel, boolean needsScrollPane) {
        this.userPanel = panel;
        this.needsScrollPane = needsScrollPane;
        return appContainer.setUserPanel(panel, needsScrollPane);
    }

    public SpecialTreeGraphics createSpecialTreeGraphics() {
        return null;
    }

    @Override
    public void nodeDoubleClicked(Node selectedNode) {

    }

    @Override
    public void nodeAboutToBeAdded(ObjectLocation homeLocation, ObjectMeta newChild) {

    }

    @Override
    public void nodesSelected(List<Node> nodes, Class commonClass) {

    }

    public List<Command> getCommands(Node node) {
        return new ArrayList<Command>();
    }

    public List<Command> getCommands(List<Node> nodes, Class commonType) {
        return new ArrayList<Command>();
    }

    public boolean nodeSelected(Node node) {
        ObjectMeta objectMeta = node.objectMeta();
        if(objectMeta.getKey()!=null) {
            appData.setLastSelected(objectMeta.getType().getSimpleName() + ":" + objectMeta.getGlobalKey());
        }
        return false;
    }

    public void nodeAboutToBeRemoved(Node node, boolean wasCut) {

    }

    public void nodeAdded(Node node) {

    }

    public void nodeUpdated(Node objectNode, Map<String, PropertyChange> changes) {

    }

    public void nodeMovedUp(Node node) {

    }

    public void nodeMovedDown(Node node) {

    }

    public void init(final ApplicationContainer<T> applicationContainer) {
        appContainer = applicationContainer;

        JMenu metaMenu = new JMenu("Meta");
        metaMenu.add(new AbstractAction("print all objects") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Collection<ObjectMeta> objects = applicationContainer.getGuiContext().getClassDatabase().allManagedObjects();
                for (ObjectMeta object : objects) {
                    System.out.println(object.meta());
                }
            }
        });
        SwingUtils.setFont(metaMenu);
        applicationContainer.getMenuBar().add(metaMenu);
        initAppData();
        selectLastEditedContent();
    }

    public ApplicationContainer<T> getAppContainer() {
        return appContainer;
    }

    public T model() {
        return appContainer.getGuiContext().getObjectMeta().getInstance();
    }

    public ClassDatabase<T> classDatabase() {
        return appContainer.getGuiContext().getClassDatabase();
    }

    @Override
    public void handleUncaughtException(Throwable e) {
        if (!handle(e)) {
            e.printStackTrace();
        }
    }

    private boolean handle(Throwable t) {
        if (t instanceof XappException) {
            SwingUtils.warnUser(appContainer.getMainFrame(), t.getMessage());
            return true;
        } else if (t.getCause() != null) {
            return handle(t.getCause());
        }
        return false;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void save() {
        getAppContainer().save();
    }

    public JComponent getUserPanel() {
        return userPanel;
    }

    public boolean userPanelNeedsScrollPane() {
        return needsScrollPane;
    }

    protected void selectLastEditedContent() {
        String lastEdited = appData.getLastSelected();
        if (lastEdited != null) {
            String[] s = lastEdited.split(":");
            ClassModel cm = classDatabase().getClassModelBySimpleName(s[0]);
            Object o = cm.getClassDatabase().getInstanceNoCheck(cm.getContainedClass(), s[1]);
            if (o != null) {
                appContainer.expand(o);
            }
        }
    }

    protected void initAppData() {
        String appDataFileName = "app-data.xml";
        final File appDataFile = new File(appContainer.getGuiContext().getCurrentFile().getParentFile(), appDataFileName);
        if (appDataFile.exists()) {
            try {
                appData = classDatabase().createUnmarshaller(AppData.class).unmarshal(appDataFile).getInstance();
                int div = appData.getDividerLocation();
                if (div != 0) {
                    appContainer.setDividerLocation(div);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                appData = new AppData();
            }
        } else {
            appData = new AppData();
        }

        appContainer.addAfterHook(DefaultAction.SAVE, new ApplicationContainer.Hook() {
            public void execute() {
                appData.setDividerLocation(appContainer.getDividerLocation());
                classDatabase().createMarshaller(AppData.class).marshal(appDataFile, appData);
            }
        });
    }
}
