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
import net.sf.xapp.objserver.apis.objmanager.to.CreateEmptyObject;
import net.sf.xapp.objserver.apis.objmanager.to.CreateObject;
import net.sf.xapp.objserver.types.AddConflict;
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
    protected final File objFile;
    protected final DeltaFile deltaFile;
    protected final DeltaFile offlineFile;
    protected final long LOCAL_ID_START = -1000000L;

    protected Class rootObjType;
    protected ObjectMeta objMeta;
    protected ClassDatabase cdb;
    private ObjectMeta lastCreated;
    private ModelProxy modelProxy;
    private MasterObjUpdater objUpdate;
    private SimpleObjUpdater localObjUpdater;
    protected long lastKnownRevision;

    public ObjClient(File localDir, String userId, HostInfo hostInfo, String appId, final String objId, Class rootObjType) {
        super(new Multicaster<ObjListener>());
        this.rootObjType = rootObjType;
        this.clientContext = new ObjClientContext(userId, new ServerProxyImpl(hostInfo));
        this.objId = objId;
        File dir = new File(new File(new File(localDir, userId), appId), objId);
        dir.mkdirs();
        objFile = new File(dir, "obj.xml");
        deltaFile = new DeltaFile(new File(dir, "deltas.txt"));
        offlineFile = new DeltaFile(new File(dir, "offline.txt"));

        /**
         * receives server updates, updates official revision, and writes the deltas
         */
        addObjListener(new ObjListenerAdaptor() {
            @Override
            public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
                if (isOnlineMode()) {
                    cdb.setRevision(ReflectionUtils.<Long>call(inMessage, "getRev"));
                    deltaFile.handleMessage(inMessage);
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

        init();
    }

    private boolean isOnlineMode() {
        return clientContext.isOnlineMode();
    }

    public void init() {
        clientContext.addConnectionListener(this);

        //if we were offline before?
        //first, can we connect immediately

        boolean connected = clientContext.connect(false);
        if(!connected) {
            initialConnectionFailed();
            loadObjMetaFromClientData(true);
            setOffline();
            objMetaLoaded_internal();
        }

    }

    protected void initialConnectionFailed() {

    }

    public void enterOfflineMode() {
        if(offlineFile.isEmpty()) {
            getCdb().setMaster(LOCAL_ID_START); //start the cdb creating its own ids
            offlineFile.reset(getLastKnownRevision());
        } else {
            long localIdStart = LOCAL_ID_START;
            for (Delta delta : offlineFile.getDeltas()) {
                InMessage message = delta.getMessage();
                if(message instanceof CreateEmptyObject || message instanceof CreateObject) {
                    localIdStart++;          //don't use local ids twice if we are going into offline mode a second time without sync
                }
            }
            getCdb().setMaster(localIdStart);
        }
    }

    public void onConnect(){
        clientContext.login();

        clientContext.wire(ObjManagerReply.class, objId, this);

        clientContext.channel(objId).join(clientContext.getUserId());

        ObjManager objManager = clientContext.objManager(objId);
        lastKnownRevision = getLastKnownRevision();

        if(lastKnownRevision != -1) {
            objManager.getDeltas(clientContext.getUserId(), lastKnownRevision, null);
        } else {
            objManager.getObject(clientContext.getUserId());
        }
    }

    /**
     * replace all client state with a fresh version of the object
     */
    public void reset(XmlObj obj) {
        FileUtils.writeFile(obj.getData(), objFile);
        deltaFile.reset(obj.getLastChangeRev());
        setObjMeta(new Unmarshaller(obj.getType()).unmarshalString(obj.getData()));
        cdb.setRevision(obj.getLastChangeRev());
    }

    public void reconstruct(List<Delta> deltas, Long revTo) {
        SimpleObjUpdater objUpdater = loadObjMetaFromClientData(false);


        //apply server updates (since our previous session ended)
        for (Delta delta : deltas) {
            delta.getMessage().visit(objUpdater);
        }
        save();
    }

    private SimpleObjUpdater loadObjMetaFromClientData(boolean applyOfflineUpdates) {
        setObjMeta(new Unmarshaller(rootObjType).unmarshal(objFile));

        SimpleObjUpdater objUpdater = new SimpleObjUpdater(objMeta, true);

        //apply local updates
        for (Delta delta: deltaFile.getDeltas()) {
            delta.getMessage().visit(objUpdater);
        }

        if(applyOfflineUpdates) {
            for (Delta delta : offlineFile.getDeltas()) {
                delta.getMessage().visit(objUpdater);
            }
        }
        return objUpdater;
    }

    private void setObjMeta(ObjectMeta objMeta) {
        this.objMeta = objMeta;
        cdb = objMeta.getClassDatabase();

        localObjUpdater.setRootObj(objMeta);
        objUpdate.init(); //must init only when the obj-meta is loaded
    }

    @Override
    public void save() {
        if (isOnlineMode()) {
            reset(SimpleObjUpdater.toXmlObj(objMeta));
        }
        //in offline mode, every change is saved anyway
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
            assert rootObjType.equals(type); //client and server must agree on the type
            reconstruct(deltas, revTo);
            //are there any offline changes?
            if(!offlineFile.isEmpty()) {
                clientContext.objManager(objId).applyChanges(clientContext.getUserId(), offlineFile.getDeltas(),
                        ConflictResolution.ABORT_ON_CONFLICT, offlineFile.getBaseRevision(), LOCAL_ID_START);
            } else {
                objMetaLoaded_internal();
            }
        } else {
            //TODO handle possible offline deltas in this scenario
            clientContext.objManager(objId).getObject(clientContext.getUserId());
        }
    }

    @Override
    public void applyChangesResponse(UserId principal, ConflictStatus conflictStatus, List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts, ErrorCode errorCode) {
         if(errorCode == null) {
             //override to
             if(wasResolved(conflictStatus)) {

                 objMetaLoaded_internal();
             } else {
                 handleConflicts(propConflicts, deleteConflicts, moveConflicts, addConflicts);
             }
         } else {
             //TODO stash the unsuccessful offline changes
         }
    }

    protected abstract void handleConflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts);

    public static boolean wasResolved(ConflictStatus conflictStatus) {
        return conflictStatus != ConflictStatus.NOTHING_COMMITTED;
    }

    public ObjectMeta getObjMeta() {
        return objMeta;
    }

    public ClassDatabase getCdb() {
        return cdb;
    }


    private void objMetaLoaded_internal() {
        if(isOnlineMode()) {
            offlineFile.delete();
        }
        objMetaLoaded();
    }

    protected abstract void objMetaLoaded();


    public static void main(final String[] args) throws ClassNotFoundException {
        final Class rootObjType = Class.forName(args[4]);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                new GUIObjClient(System.getProperty("user.home", ".") + "/xapp-cache", args[0], HostInfo.parse(args[1]), args[2], args[3], rootObjType);

            }
        });
    }

    public ObjClientContext getClientContext() {
        return clientContext;
    }

    public String getObjId() {
        return objId;
    }


    public File getObjFile() {
        return objFile;
    }

    public DeltaFile getDeltaFile() {
        return deltaFile;
    }


    public ObjectMeta getLastCreated() {
        return lastCreated;
    }

    public void close() {
        clientContext.setOffline();
        deltaFile.close();
        offlineFile.close();
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
            case CONNECTION_LOST:
                enterOfflineMode();
                break;
            case CONNECTING:
                break;
        }
    }

    @Override
    public void handleConnectException(Exception e) {

    }

    public void setOffline() {
        save(); //we may as well save a fresh file here, so we have no deltas to complicate things
        clientContext.setOffline();
    }

    public void connect() {
        clientContext.connect(true);
    }

    public ObjUpdate getObjUpdate() {
        return objUpdate;
    }

    public UserId getUserId() {
        return clientContext.getUserId();
    }

    /**
     * responsible for handling online (sends to server) and local (from UI, or model proxy) updates
     */
    public long getLastKnownRevision() {
        // if deltas exist then parse them, and use the last one to get the last known rev
        return deltaFile.getLastRevision();
    }

    public void addObjListener(ObjListener objListener) {
        ((Multicaster<ObjListener>) delegate).addDelegate(objListener);
    }

    public DeltaFile getOfflineFile() {
        return offlineFile;
    }

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
                offlineFile.handleMessage(inMessage);
                //apply the change
                inMessage.visit(dummyServer);
            }
            return null;
        }
    }
}
