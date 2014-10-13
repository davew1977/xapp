package net.sf.xapp.objclient.localstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.client.io.ServerProxyImpl;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objclient.IncomingChangesAdaptor;
import net.sf.xapp.objclient.NodeUpdateApiRemote;
import net.sf.xapp.objclient.ObjClientContext;
import net.sf.xapp.objclient.ui.ChatPane;
import net.sf.xapp.objcommon.SimpleObjUpdater;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;
import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.ant.AntFacade;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class ObjClient extends ObjListenerAdaptor implements SaveStrategy, ObjManagerReply {
    public static final String LOCAL_DIR = System.getProperty("user.home", ".") + "/xapp-cache";
    private final ObjClientContext clientContext;
    private final String objId;
    private final File revFile;
    private final File objFile;
    private final File deltaFile;
    private final List<Delta> initialDeltas;
    private OutputStreamWriter deltaWriter;

    private ObjectMeta objMeta;
    private ClassDatabase cdb;

    public ObjClient(String userId, HostInfo hostInfo, String appId, String objId) {
        this.clientContext = new ObjClientContext(userId, new ServerProxyImpl(hostInfo));
        this.objId = objId;
        File dir = new File(new File(new File(LOCAL_DIR, userId), appId), objId);
        dir.mkdirs();
        revFile = new File(dir, "rev.txt");
        objFile = new File(dir, "obj.xml");
        deltaFile = new File(dir, "deltas.txt");
        initialDeltas = readDeltas();

        init();
    }

    public void init() {
        clientContext.connect();
        clientContext.login();

        clientContext.wire(ObjManagerReply.class, objId, this);

        clientContext.channel(objId).join(clientContext.getUserId());

        ObjManager objManager = clientContext.objManager(objId);
        long rev = getLastKnownRevision();
        if(rev != -1) {
            objManager.getDeltas(clientContext.getUserId(), rev, null);
        } else {
            objManager.getObject(clientContext.getUserId());
        }
    }

    public long getLastKnownRevision() {
        // if deltas exist then parse them, and use the last one to get the last known rev
        if(!initialDeltas.isEmpty()) {
            InMessage message = initialDeltas.get(initialDeltas.size() - 1).getMessage();
            return ReflectionUtils.call(message, "getRev");
        } else if(revFile.exists()) {
            return Long.parseLong(FileUtils.readFile(revFile).split("\n")[0]);
        } else {
            return -1;
        }
    }

    @Override
    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
        write(new Delta(inMessage).serialize(), getDeltaWriter());
        return null;
    }

    /**
     * replace all client state with a fresh version of the object
     */
    public void reset(XmlObj obj) {
        closeDeltaWriter();
        new AntFacade().deleteFile(deltaFile);
        FileUtils.writeFile(obj.getData(), objFile);
        FileUtils.writeFile(obj.getLastChangeRev(), revFile);
        Unmarshaller unmarshaller = new Unmarshaller(obj.getType());
        objMeta = unmarshaller.unmarshalString(obj.getData());
        cdb = unmarshaller.getClassDatabase();
        cdb.setRevision(obj.getLastChangeRev());
    }

    public void reconstruct(Class type, List<Delta> deltas, Long revTo) {
        Unmarshaller unmarshaller = new Unmarshaller(type);
        objMeta = unmarshaller.unmarshal(objFile);
        cdb = unmarshaller.getClassDatabase();

        SimpleObjUpdater objUpdater = new SimpleObjUpdater(objMeta);

        //apply local updates
        for (Delta delta: initialDeltas) {
            delta.getMessage().visit(objUpdater);
        }
        //apply server updates (since our previous session ended)
        for (Delta delta : deltas) {
            delta.getMessage().visit(objUpdater);
        }
        save();
    }

    @Override
    public void save() {
        reset(SimpleObjUpdater.toXmlObj(objMeta));
    }

    private void closeDeltaWriter() {
        try {
            getDeltaWriter().flush();
            getDeltaWriter().close();
            deltaWriter = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputStreamWriter getDeltaWriter() {
        if (deltaWriter == null) {
            try {
                deltaWriter = new OutputStreamWriter(new FileOutputStream(deltaFile, true), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return deltaWriter;
    }

    public ObjectMeta getObjMeta() {
        return objMeta;
    }

    public ClassDatabase getCdb() {
        return cdb;
    }

    @Override
    public void getObjectResponse(UserId principal, XmlObj obj, ErrorCode errorCode) {

        System.out.println(obj.getData());

        reset(obj);
        launchGUI();
    }


    @Override
    public void getDeltasResponse(UserId principal, List<Delta> deltas, Class type, Long revTo, ErrorCode errorCode) {
        if(errorCode==null) {
            reconstruct(type, deltas, revTo);
            launchGUI();
        } else {
            clientContext.objManager(objId).getObject(clientContext.getUserId());
        }
    }

    private void launchGUI() {
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

        IncomingChangesAdaptor incomingChangesAdaptor = new IncomingChangesAdaptor(appContainer, clientContext);
        appContainer.setNodeUpdateApi(new NodeUpdateApiRemote(cdb, clientContext, objId, incomingChangesAdaptor));
        clientContext.wire(ObjListener.class, objId, incomingChangesAdaptor);
        clientContext.wire(ObjListener.class, objId, this);
    }

    private List<Delta> readDeltas() {              // todo read and parse deltas
        List<Delta> deltas = new ArrayList<Delta>();
        if (deltaFile.exists()) {
            String[] lines = FileUtils.readFile(deltaFile, Charset.forName("UTF-8")).split("\n");
            for (String line : lines) {
                deltas.add(new Delta().deserialize(line));
            }
        }
        return deltas;
    }

    private void write(String line, Writer writer) {
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new ObjClient(args[0], HostInfo.parse(args[1]), args[2], args[3]);
    }
}
