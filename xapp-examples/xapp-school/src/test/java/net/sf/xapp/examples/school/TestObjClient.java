package net.sf.xapp.examples.school;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.xapp.application.api.ModelProxy;
import net.sf.xapp.examples.school.model.Pupil;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.util.ReflectionUtils;
import net.sf.xapp.net.testharness.TestMessageHandler;
import net.sf.xapp.objclient.ObjClient;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReplyAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.apis.objmanager.to.GetDeltasResponse;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;

import static java.util.Arrays.asList;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class TestObjClient implements ModelProxy{

    private ObjClient objClient;
    private CountDownLatch initSignal = new CountDownLatch(1);
    private TestMessageHandler messageHandler = new TestMessageHandler();


    public TestObjClient(File localDir, String userId, String appId, String objId) {
        objClient = new ObjClient(localDir, userId, HostInfo.parse("11375"), appId, objId) {
            @Override
            protected void preInit() {
                clientContext.wire(ObjManagerReply.class, objId, new ObjManagerReplyAdaptor(messageHandler));
                clientContext.wire(ObjListener.class, objId, new ObjListenerAdaptor(messageHandler));
            }

            @Override
            protected void objMetaLoaded() {
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
    public <T> void add(Object parent, String property, T obj) {
        getModelProxy().add(parent, property, obj);
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
    public <T> T checkout(Class<T> type, Long id) {
        return getModelProxy().checkout(type, id);
    }

    @Override
    public <T> void commit(T obj) {
        getModelProxy().commit(obj);
    }

    @Override
    public <T> void cancelCheckout(T obj) {
        getModelProxy().cancelCheckout(obj);
    }

    @Override
    public <T> void delete(T obj) {
        getModelProxy().delete(obj);
    }

    public ObjectMeta lastCreated() {
        return objClient.getLastCreated();
    }
}
