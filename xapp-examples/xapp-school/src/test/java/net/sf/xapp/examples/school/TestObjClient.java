package net.sf.xapp.examples.school;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.util.ReflectionUtils;
import net.sf.xapp.objclient.ObjClient;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;

import static java.util.Arrays.asList;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class TestObjClient extends ObjUpdateAdaptor {

    private ObjClient objClient;
    private ObjUpdate remoteServer;
    private CountDownLatch initSignal = new CountDownLatch(1);
    private CountDownLatch syncSignal;

    public TestObjClient(File localDir, String userId, String appId, String objId) {
        objClient = new ObjClient(localDir, userId, HostInfo.parse("11375"), appId, objId) {
            @Override
            protected void objMetaLoaded() {
                initSignal.countDown();
                objClient.getClientContext().wire(ObjListener.class, objId, new ObjListenerAdaptor() {
                    @Override
                    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
                        UserId u = (UserId) ReflectionUtils.call(inMessage, "getUser");
                        if(objClient.getClientContext().getUserId().equals(u)) {
                            syncSignal.countDown();
                        }
                        return null;
                    }
                });
            }
        };

        remoteServer = objClient.getClientContext().objUpdate(objId);
    }

    public void waitUntilInitialized() throws InterruptedException {
        initSignal.await();
    }

    public void close() {
        objClient.getClientContext().disconnect();
    }

    @Override
    public <T> T handleMessage(InMessage<ObjUpdate, T> inMessage) {
        inMessage.visit(remoteServer);
        syncSignal = new CountDownLatch(1);
        try {
            syncSignal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ClassDatabase getCdb() {
        return objClient.getCdb();
    }

    public File getRevFile() {
        return objClient.getRevFile();
    }

    public File getObjFile() {
        return objClient.getObjFile();
    }

    public File getDeltaFile() {
        return objClient.getDeltaFile();
    }

    public List<Delta> readDeltas() {
        return objClient.readDeltas();
    }

    public ObjectMeta getObjMeta() {
        return objClient.getObjMeta();
    }

    public UserId getUserId() {
        return objClient.getClientContext().getUserId();
    }

    public void updateObject(ObjectMeta objectMeta, String propName, String value) {
        updateObject(getUserId(), asList(new PropChangeSet(objectMeta.getId(), asList(new PropChange(propName, null, value)))));
    }

    public ObjClient getObjClient() {
        return objClient;
    }
}
