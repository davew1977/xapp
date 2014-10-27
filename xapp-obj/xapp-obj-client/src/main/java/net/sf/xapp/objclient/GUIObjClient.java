package net.sf.xapp.objclient;

import java.io.File;

import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
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
    public GUIObjClient(String localDir, String userId, HostInfo hostInfo, String appId, String objId) {
        super(new File(localDir), userId, hostInfo, appId, objId);
    }

    @Override
    protected void objMetaLoaded() {

        final ChatPane chatPane = new ChatPane(clientContext);
        clientContext.wire(ChatClient.class, objId, chatPane);
        clientContext.wire(ChatUser.class, objId, chatPane);
        final ChatApp chatApp = clientContext.chatApp(objId);
        chatPane.addListener(new Callback() {
            @Override
            public <T> T call(Object... args) {
                chatApp.newChatMessage(clientContext.getUserId(), (String) args[0]);
                return null;
            }
        });
        chatPane.setSize(200, 300);

        ApplicationContainerImpl appContainer = new ApplicationContainerImpl(new DefaultGUIContext(new File("file.xml"), cdb, objMeta));
        appContainer.setSaveStrategy(this);
        appContainer.add(chatPane, "bottomLeft");
        appContainer.setUserGUI(new SimpleApplication());
        appContainer.getMainFrame().setVisible(true);


        clientContext.wire(ObjListener.class, objId, new UIUpdater(cdb, appContainer));
        appContainer.setNodeUpdateApi(new NodeUpdateApiRemote(cdb, clientContext, objId));

    }
}
