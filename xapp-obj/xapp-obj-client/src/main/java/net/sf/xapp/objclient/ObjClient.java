package net.sf.xapp.objclient;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.application.api.ModelProxy;
import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.client.io.ConnectionListener;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.client.io.ServerProxyImpl;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.Multicaster;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objcommon.SimpleObjUpdater;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.types.ConflictResolution;
import net.sf.xapp.objserver.types.MoveConflict;
import net.sf.xapp.objserver.types.PropConflict;
import net.sf.xapp.objserver.types.ConflictStatus;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.DeleteConflict;
import net.sf.xapp.objserver.types.XmlObj;
import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.ant.AntFacade;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public abstract class ObjClient extends ObjListenerAdaptor implements SaveStrategy, ObjManagerReply, ConnectionListener {
    protected final ObjClientContext clientContext;
    protected final String objId;
    protected final File revFile;
    protected final File objFile;
    protected final File deltaFile;
    protected final File offlineFile;
    protected final List<Delta> initialDeltas;
    private final long LOCAL_ID_START = -1000000L;
    private OutputStreamWriter deltaWriter;
    private OutputStreamWriter offlineWriter;

    protected ObjectMeta objMeta;
    protected ClassDatabase cdb;
    private ObjectMeta lastCreated;
    private ModelProxy modelProxy;
    private MasterObjUpdater objUpdate;
    private SimpleObjUpdater localObjUpdater;
    protected List<Delta> offlineDeltas;
    protected String localSnapshot; //store a version of the object before server deltas are applied

    public ObjClient(File localDir, String userId, HostInfo hostInfo, String appId, final String objId) {
        super(new Multicaster<ObjListener>());
        this.clientContext = new ObjClientContext(userId, new ServerProxyImpl(hostInfo));
        this.objId = objId;
        File dir = new File(new File(new File(localDir, userId), appId), objId);
        dir.mkdirs();
        revFile = new File(dir, "rev.txt");
        objFile = new File(dir, "obj.xml");
        deltaFile = new File(dir, "deltas.txt");
        offlineFile = new File(dir, "offline.txt");
        initialDeltas = readDeltas();


        /**
         * receives server updates, updates official revision, and writes the deltas
         */
        addObjListener(new ObjListenerAdaptor() {
            @Override
            public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
                if (isOnlineMode()) {
                    cdb.setRevision(ReflectionUtils.<Long>call(inMessage, "getRev"));
                    write(new Delta(inMessage, 0L).serialize(), getDeltaWriter());
                }
                return null;
            }
        });

        localObjUpdater = new SimpleObjUpdater(null, false) {
            @Override
            protected void objAdded(UserId principal, ObjLoc objLoc, ObjectMeta objectMeta) {
                ObjClient.this.lastCreated = objectMeta;
            }
        };
        addObjListener(localObjUpdater);

        objUpdate = new MasterObjUpdater(objId);

        modelProxy = new ModelProxyImpl(this);

        preInit();
        init();
    }

    private boolean isOnlineMode() {
        return clientContext.isOnlineMode();
    }

    public void init() {
        clientContext.addConnectionListener(this);

        //if we were offline before?
        //first, can we connect immediately

        clientContext.setReconnect(false);
        boolean connected = clientContext.connect();
        if(!connected) {
            initialConnectionFailed();
            enterOfflineMode();
        }

    }

    protected void initialConnectionFailed() {

    }

    public void enterOnlineMode() {

    }

    public void enterOfflineMode() {
       getCdb().setMaster(LOCAL_ID_START); //start the cdb creating its own ids
    }

    public void onConnect(){
        clientContext.setReconnect(true);
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
            return (Long) ReflectionUtils.call(message, "getRev");
        } else if(revFile.exists()) {
            return Long.parseLong(FileUtils.readFile(revFile).split("\n")[0]);
        } else {
            return -1;
        }
    }

    /**
     * replace all client state with a fresh version of the object
     */
    public void reset(XmlObj obj) {
        closeDeltaWriter();
        closeOfflineWriter();
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

        SimpleObjUpdater objUpdater = new SimpleObjUpdater(objMeta, true);

        //apply local updates
        for (Delta delta: initialDeltas) {
            delta.getMessage().visit(objUpdater);
        }

        localSnapshot = objMeta.toXml();

        //apply server updates (since our previous session ended)
        for (Delta delta : deltas) {
            delta.getMessage().visit(objUpdater);
        }
        save();
    }

    @Override
    public void save() {
        if (isOnlineMode()) {
            reset(SimpleObjUpdater.toXmlObj(objMeta));
        }
        //in offline mode, every change is saved anyway
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
    private void closeOfflineWriter() {
        try {
            getOfflineWriter().flush();
            getOfflineWriter().close();
            offlineWriter = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getObjectResponse(UserId principal, XmlObj obj, ErrorCode errorCode) {
        clientContext.wire(ObjListener.class, objId, this);
        reset(obj);
        objMetaLoaded_internal();
    }

    @Override
    public void getDeltasResponse(UserId principal, List<Delta> deltas, Class type, Long revTo, ErrorCode errorCode) {
        if(errorCode==null) {
            clientContext.wire(ObjListener.class, objId, this);
            reconstruct(type, deltas, revTo);
            //are there any offline changes?
            offlineDeltas = readOffline();
            if(!offlineDeltas.isEmpty()) {
                clientContext.objManager(objId).applyChanges(clientContext.getUserId(), offlineDeltas,
                        ConflictResolution.ABORT_ON_CONFLICT, objMeta.getRevision(), LOCAL_ID_START);
            } else {
                objMetaLoaded_internal();
            }
        } else {
            //TODO handle possible offline deltas in this scenario
            clientContext.objManager(objId).getObject(clientContext.getUserId());
        }
    }

    @Override
    public final void applyChangesResponse(UserId principal, ConflictStatus conflictStatus, List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, ErrorCode errorCode) {
         if(errorCode == null) {
             //override to
             if(wasResolved(conflictStatus)) {
                 objMetaLoaded_internal();
             } else {
                 handleConflicts(propConflicts, deleteConflicts, moveConflicts);
             }
         } else {
             //TODO stash the unsuccessful offline changes
         }
    }

    protected abstract void handleConflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts);

    public static boolean wasResolved(ConflictStatus conflictStatus) {
        return conflictStatus != ConflictStatus.NOTHING_COMMITTED;
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

    private OutputStreamWriter getOfflineWriter() {
        if (offlineWriter == null) {
            try {
                offlineWriter = new OutputStreamWriter(new FileOutputStream(offlineFile, true), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return offlineWriter;
    }

    public ObjectMeta getObjMeta() {
        return objMeta;
    }


    public ClassDatabase getCdb() {
        return cdb;
    }

    private void objMetaLoaded_internal() {
        localObjUpdater.setRootObj(objMeta);
        objUpdate.init(); //must init only when the obj-meta is loaded
        objMetaLoaded();
    }

    protected abstract void objMetaLoaded();
    protected abstract void preInit();

    public List<Delta> readDeltas() {
        List<Delta> deltas = new ArrayList<Delta>();
        if (deltaFile.exists()) {
            String[] lines = FileUtils.readFile(deltaFile, Charset.forName("UTF-8")).split("\n");
            for (String line : lines) {
                deltas.add(new Delta().deserialize(line));
            }
        }
        return deltas;
    }

    public List<Delta> readOffline() {
        List<Delta> deltas = new ArrayList<Delta>();
        if (offlineFile.exists()) {
            String[] lines = FileUtils.readFile(offlineFile, Charset.forName("UTF-8")).split("\n");
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

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new GUIObjClient(System.getProperty("user.home", ".") + "/xapp-cache", args[0], HostInfo.parse(args[1]), args[2], args[3]);

            }
        });
    }

    public ObjClientContext getClientContext() {
        return clientContext;
    }

    public String getObjId() {
        return objId;
    }

    public File getRevFile() {
        return revFile;
    }

    public File getObjFile() {
        return objFile;
    }

    public File getDeltaFile() {
        return deltaFile;
    }

    public List<Delta> getInitialDeltas() {
        return initialDeltas;
    }

    public ObjectMeta getLastCreated() {
        return lastCreated;
    }

    public void close() {
        clientContext.setOffline();
        closeDeltaWriter();
        closeOfflineWriter();
    }

    public ModelProxy getModelProxy() {
        return modelProxy;
    }

    public boolean isConnected() {
        return clientContext.isConnected();
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        clientContext.addConnectionListener(connectionListener);
    }

    @Override
    public void connectionStateChanged(ConnectionState newState) {
        switch (newState) {
            case ONLINE:
                onConnect();
                break;
            case OFFLINE:
            case CONNECTING:
            case CONNECTION_LOST:
                enterOfflineMode();
                break;
        }
    }

    @Override
    public void handleConnectException(Exception e) {

    }

    public void setOffline() {
        clientContext.setOffline();
    }

    public void connect() {
        clientContext.connect();
    }

    public ObjUpdate getObjUpdate() {
        return objUpdate;
    }

    public UserId getUserId() {
        return clientContext.getUserId();
    }

    public void addObjListener(ObjListener objListener) {
        ((Multicaster<ObjListener>) delegate).addDelegate(objListener);
    }

    /**
     * responsible for handling online (sends to server) and local (from UI, or model proxy) updates
     */
    private class MasterObjUpdater extends ObjUpdateAdaptor {
        ObjUpdate remote;
        LiveObject dummyServer;

        public MasterObjUpdater(String objId) {
            remote = clientContext.objUpdate(objId);
        }

        public void init() {
            dummyServer = new LiveObject(objMeta);
            dummyServer.addListener(ObjClient.this);
        }

        @Override
        public <T> T handleMessage(InMessage<ObjUpdate, T> inMessage) {
            if(isOnlineMode()) {
                inMessage.visit(remote);
            } else {
                write(new Delta(inMessage, System.currentTimeMillis()).serialize(), getOfflineWriter());
                //apply the change
                inMessage.visit(dummyServer);
            }
            return null;
        }
    }
}
