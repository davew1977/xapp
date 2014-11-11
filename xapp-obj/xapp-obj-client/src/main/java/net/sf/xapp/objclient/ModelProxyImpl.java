package net.sf.xapp.objclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import net.sf.xapp.application.api.ModelProxy;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.util.ReflectionUtils;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class ModelProxyImpl extends ObjUpdateAdaptor implements ModelProxy{
    private ClassDatabase cdb;
    private CountDownLatch syncSignal;
    private ObjUpdate remoteServer;
    private ObjClient objClient;
    private Map<Long, Map<Property, String>> snapshots = new HashMap<Long, Map<Property, String>>();

    public ModelProxyImpl(final ObjClient objClient) {
        this.cdb = objClient.cdb;
        this.objClient = objClient;
        String objId = objClient.getObjId();
        remoteServer = objClient.getClientContext().objUpdate(objId);
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

    @Override
    public <T> void add(Object parent, String property, T obj) {
        createObject(userId(), new ObjLoc(cdb.find(parent).getId(), property, -1), obj.getClass(), cdb.createMarshaller(obj.getClass()).toXMLString(obj));
    }

    @Override
    public <T> T create(Object parent, String property, Class<T> type) {
        return create(cdb.find(parent).getId(), property, type);
    }

    @Override
    public <T> T create(Long parentId, String property, Class<T> type) {
        createEmptyObject(userId(), new ObjLoc(parentId, property, -1), type);//blocking call
        return (T) checkout(lastCreated().getInstance());
    }

    @Override
    public <T> T checkout(T obj) {
        ObjectMeta objectMeta = cdb.find(obj);
        snapshots.put(objectMeta.getId(), objectMeta.snapshot());
        return obj;
    }

    @Override
    public <T> T checkout(Class<T> type, Long id) {
        ObjectMeta<T> objectMeta = cdb.findObjById(id);
        snapshots.put(objectMeta.getId(), objectMeta.snapshot());
        return objectMeta.getInstance();
    }

    @Override
    public <T> void commit(T obj) {
        ObjectMeta<?> objectMeta = cdb.find(obj);
        Map<Property, String> previous = snapshots.remove(objectMeta.getId());
        Map<Property, String> snapshot = objectMeta.snapshot();
        List<Property> properties = objectMeta.getProperties();
        List<PropChange> changes = new ArrayList<PropChange>();
        for (Property property : properties) {
            String oldVal = previous.get(property);
            String newVal = snapshot.get(property);
            if(!Property.objEquals(oldVal, newVal)) {
                changes.add(new PropChange(property.getName(), oldVal, newVal));
            }
        }
        if (!changes.isEmpty()) {
            List<PropChangeSet> changeSets = new ArrayList<PropChangeSet>();
            changeSets.add(new PropChangeSet(objectMeta.getId(), changes));
            updateObject(userId(), changeSets);
        }
    }

    @Override
    public <T> void cancelCheckout(T obj) {
        snapshots.remove(cdb.find(obj).getId());
    }

    @Override
    public <T> void delete(T obj) {
        deleteObject(userId(), cdb.find(obj).getId());
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

    public ObjectMeta lastCreated() {
        return objClient.getLastCreated();
    }

    private UserId userId() {
        return objClient.getClientContext().getUserId();
    }
}
