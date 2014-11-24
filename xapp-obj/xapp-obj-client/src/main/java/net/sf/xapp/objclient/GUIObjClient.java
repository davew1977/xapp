package net.sf.xapp.objclient;

import java.io.File;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.objclient.ui.ChatPane;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class GUIObjClient extends ObjClient {
    private ApplicationContainerImpl appContainer;
    private UIUpdater uiUpdater;
    private final ChatPane chatPane;

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

    public ApplicationContainer getAppContainer() {
        return appContainer;
    }
}
