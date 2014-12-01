package net.sf.xapp.objclient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.objclient.ui.ChatPane;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.ConflictStatus;
import net.sf.xapp.objserver.types.DeleteConflict;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.MoveConflict;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropConflict;
import net.sf.xapp.utils.StringUtils;

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

    @Override
    protected void handleConflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts) {
        //local cdb for context on deleted objects
        ClassDatabase localCdb = new Unmarshaller(objMeta.getType()).unmarshalString(localSnapshot).getClassDatabase();

        JTextArea textarea = new JTextArea();
        textarea.setWrapStyleWord(true);
        textarea.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(textarea);
        jsp.setPreferredSize(new Dimension(300,200));

        StringBuilder sb = new StringBuilder();
        if(!propConflicts.isEmpty()) {
            title(sb, "Property Conflicts (you tried to change an object property that was changed on the server)");
            for (PropConflict propConflict : propConflicts) {
                Long id = propConflict.getPropChange().getObjId();
                ObjectMeta objectMeta = localCdb.find(id);
                sb.append(String.format("%s : %s : %s\n", objectMeta.getType().getSimpleName(), objectMeta.getKey(), id));
                PropChange change = propConflict.getPropChange().getChange();
                Delta myDelta = offlineDeltas.get(propConflict.getMyDeltaIndex());
                sb.append(String.format("\ttheirs: \"%s\" changed from \"%s\" to \"%s\"", change.getProperty(), change.getOldValue(), change.getNewValue()));
                sb.append(String.format("\tyours:  \"%s\" changed from \"%s\" to \"%s\"", change.getProperty(), change.getOldValue(), change.getNewValue()));
            }
        }
        if(!moveConflicts.isEmpty()) {
            title(sb, "Move Conflicts (you tried to move an object that has been moved on the server)");

        }
        if(!deleteConflicts.isEmpty()) {
            title(sb, "Delete Conflicts (you modified an object that no longer exists on the server)");

        }
        textarea.setText(sb.toString());
    }

    private void title(StringBuilder sb, String title) {
        sb.append(title).append("\n");
        sb.append(StringUtils.line(title.length(), '=')).append("\n").append("\n");
    }

    public ApplicationContainer getAppContainer() {
        return appContainer;
    }
}
