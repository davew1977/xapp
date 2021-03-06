package net.sf.xapp.examples.school;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.xapp.application.api.ModelProxy;
import net.sf.xapp.examples.school.model.SchoolSystem;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.testharness.TestMessageHandler;
import net.sf.xapp.objclient.DeltaFile;
import net.sf.xapp.objclient.ObjClient;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReplyAdaptor;
import net.sf.xapp.objserver.types.*;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class TestObjClient implements ModelProxy{

    private ObjClient objClient;
    private CountDownLatch initSignal = new CountDownLatch(1);
    private TestMessageHandler messageHandler = new TestMessageHandler();
    private Conflicts conflicts;


    public TestObjClient(File localDir, String userId, String appId, String objId, final String hostPort) {
        objClient = new ObjClient(localDir, userId, HostInfo.parse(hostPort), appId, objId, SchoolSystem.class) {
            @Override
            public void init() {
                addObjListener(new ObjListenerAdaptor(messageHandler));
                clientContext.wire(ObjManagerReply.class, objId, new ObjManagerReplyAdaptor(messageHandler));
                super.init();
            }

            @Override
            protected void objMetaLoaded() {
                initSignal.countDown();
            }

            @Override
            protected void handleConflicts(List<PropConflict> propConflicts, List<DeleteConflict> deleteConflicts, List<MoveConflict> moveConflicts, List<AddConflict> addConflicts) {
                conflicts = new Conflicts(propConflicts, deleteConflicts, moveConflicts, addConflicts);
                initSignal.countDown();
            }
        };
    }

    public void waitUntilInitialized() throws InterruptedException {
        initSignal.await();
    }

    public void close() {
        objClient.close();
    }

    public ClassDatabase getCdb() {
        return objClient.getCdb();
    }


    public File getObjFile() {
        return objClient.getObjFile();
    }

    public DeltaFile getDeltaFile() {
        return objClient.getDeltaFile();
    }


    public ObjectMeta getObjMeta() {
        return objClient.getObjMeta();
    }

    public UserId getUserId() {
        return objClient.getClientContext().getUserId();
    }

    public ObjClient getObjClient() {
        return objClient;
    }

    public <T> T waitFor(MessageTypeEnum messageTypeEnum, Object... propValuePairs) throws InterruptedException {
        return (T) messageHandler.waitFor(messageTypeEnum, propValuePairs);
    }

    public ModelProxy getModelProxy() {
        return objClient.getModelProxy();
    }

    @Override
    public <T> T add(Object parent, String property, T obj) {
        return getModelProxy().add(parent, property, obj);
    }

    @Override
    public <T> T getModel() {
        return getModelProxy().getModel();
    }

    @Override
    public void moveTo(Object parent, String property, Object objectToMove) {
        getModelProxy().moveTo(parent, property, objectToMove);
    }

    @Override
    public <T> T create(Object parent, String property, Class<T> type) {
        return getModelProxy().create(parent, property, type);
    }

    @Override
    public <T> T create(Long parentId, String property, Class<T> type) {
        return getModelProxy().create(parentId, property, type);
    }

    @Override
    public <T> T checkout(T obj) {
        return getModelProxy().checkout(obj);
    }

    @Override
    public <T> T checkout(Long id) {
        return getModelProxy().checkout(id);
    }

    @Override
    public void commit(Object... obj) {
        getModelProxy().commit(obj);
    }

    @Override
    public void moveInList(Object parent, String property, Object objectToMove, int delta) {
        getModelProxy().moveInList(parent, property, objectToMove, delta);
    }

    @Override
    public <T> void cancelCheckout(T obj) {
        getModelProxy().cancelCheckout(obj);
    }

    @Override
    public <T> void delete(T obj) {
        getModelProxy().delete(obj);
    }

    @Override
    public void addRefs(Object parent, String property, Object... objectsToAdd) {
        getModelProxy().addRefs(parent, property, objectsToAdd);
    }

    @Override
    public void removeRefs(Object parent, String property, Object... objectsToRemove) {
        getModelProxy().removeRefs(parent, property, objectsToRemove);
    }

    @Override
    public void changeType(Object obj, Class newType) {
        getModelProxy().changeType(obj, newType);
    }

    public ObjectMeta lastCreated() {
        return objClient.getLastCreated();
    }

    @Override
    public <T> T add(Object parent, T obj) {
        return getModelProxy().add(parent, obj);
    }

    @Override
    public <T> T create(Object parent, Class<T> type) {
        return getModelProxy().create(parent, type);
    }

    @Override
    public <T> T create(Long parentId, Class<T> type) {
        return getModelProxy().create(parentId, type);
    }

    @Override
    public void moveInList(Object parent, Object objectToMove, int delta) {
        getModelProxy().moveInList(parent, objectToMove, delta);
    }

    @Override
    public void addRefs(Object parent, Object... objectsToAdd) {
        getModelProxy().addRefs(parent, objectsToAdd);
    }

    @Override
    public void removeRefs(Object parent, Object... objectsToRemove) {
        getModelProxy().removeRefs(parent, objectsToRemove);
    }

    @Override
    public void moveTo(Object parent, Object objectToMove) {
        getModelProxy().moveTo(parent, objectToMove);
    }

    public long getLastKnownRevision() {
        return getObjClient().getLastKnownRevision();
    }

    public void setOffline() {
        getObjClient().setOffline();
    }

    public void connect() throws InterruptedException {
        getObjClient().getClientContext().wire(ObjManagerReply.class, getObjClient().getObjId(), new ObjManagerReplyAdaptor(messageHandler));
        initSignal = new CountDownLatch(1);
        getObjClient().connect();
        waitUntilInitialized();
    }

    public Conflicts getConflicts() {
        return conflicts;
    }

    public void applyChanges(ConflictResolution decision) {
        getObjClient().applyChanges(decision);
    }
}
