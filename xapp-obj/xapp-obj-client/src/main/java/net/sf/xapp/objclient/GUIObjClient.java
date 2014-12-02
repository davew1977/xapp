package net.sf.xapp.objclient;

import javax.swing.*;
import java.io.File;
import java.util.List;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.objclient.ui.ChatPane;
import net.sf.xapp.objclient.ui.ConflictHelper;
import net.sf.xapp.objserver.types.AddConflict;
import net.sf.xapp.objserver.types.DeleteConflict;
import net.sf.xapp.objserver.types.MoveConflict;
import net.sf.xapp.objserver.types.PropConflict;

import static java.lang.String.format;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class GUIObjClient extends ObjClient {
    private ApplicationContainerImpl appContainer;
    private UIUpdater uiUpdater;
    private final ChatPane chatPane;
    private JFrame conflictFrame;

    /**
     * must be called on the event dispatch thread!
     */
    public GUIObjClient(String localDir, String userId, HostInfo hostInfo, String appId, String objId) {
        super(new File(localDir), userId, hostInfo, appId, objId);
        uiUpdater = new UIUpdater(this);
        chatPane = new ChatPane(clientContext);
        final ChatApp chatApp = clientContext.chatApp(objId);
        chatPane.addListener(new Callback() {
            @Override
            public <T> T call(Object... args) {
                chatApp.newChatMessage(clientContext.getUserId(), (String) args[0]);
                return null;
            }
        });
        chatPane.setSize(200, 300);
    }

    @Override
    protected void initialConnectionFailed() {
        SwingUtils.warnUser(null, "Could not connect to server.\nWill proceed in offline mode");
    }

    @Override
    protected void objMetaLoaded() {
        clientContext.wire(ChatClient.class, objId, chatPane);
        clientContext.wire(ChatUser.class, objId, chatPane);

        if(appContainer == null) {
            appContainer = new ApplicationContainerImpl(new DefaultGUIContext(new File("file.xml"), cdb, objMeta));
            appContainer.setSaveStrategy(this);
            appContainer.add(chatPane, "bottomLeft");
            appContainer.setUserGUI(new OnlineApp(this));
            appContainer.getMainFrame().setVisible(true);
            appContainer.setNodeUpdateApi(new NodeUpdateApiRemote(cdb, this));
            addObjListener(uiUpdater);
        } else {
            appContainer.resetUI(objMeta);
        }

    }

    @Override
    protected void preInit() {

    }

    @Override
    protected void handleConflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts) {
        conflictFrame = ConflictHelper.showConflicts(offlineDeltas, propConflicts, deleteConflicts, moveConflicts, addConflicts);

    }

    public ApplicationContainer getAppContainer() {
        return appContainer;
    }

}
