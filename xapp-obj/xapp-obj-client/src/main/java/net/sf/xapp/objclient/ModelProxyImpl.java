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
import net.sf.xapp.objectmodelling.core.filters.PropertyFilter;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.apis.objmanager.to.ChangeType;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.utils.Filter;

/**
 * Manages updates to the remote object model
 */
public class ModelProxyImpl extends ObjUpdateAdaptor implements ModelProxy{
    private ClassDatabase cdb;
    private CountDownLatch syncSignal;
    private ObjUpdate remoteServer;
    private ObjClient objClient;
    private Map<Long, Map<String, Object>> snapshots = new HashMap<Long, Map<String, Object>>();

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
    public <T> T add(Object parent, T obj) {
        return add(parent, null, obj);
    }
    @Override
    public <T> T add(Object parent, String property, T obj) {
        createObject(userId(), createObjLoc(parent, property, obj.getClass(), PropertyFilter.COMPLEX_NON_REFERENCE), obj.getClass(), cdb.createMarshaller(obj.getClass()).toXMLString(obj));
        return (T) lastCreated().getInstance();
    }

    @Override
    public <T> T create(Object parent, Class<T> type) {
        return create(parent, null, type);
    }

    @Override
    public <T> T create(Object parent, String property, Class<T> type) {
        createEmptyObject(userId(), createObjLoc(parent, property, type, PropertyFilter.COMPLEX_NON_REFERENCE), type);//blocking call
        return (T) checkout(lastCreated().getInstance());
    }

    @Override
    public <T> T create(Long parentId, Class<T> type) {
        return create(parentId, null, type);
    }

    @Override
    public <T> T create(Long parentId, String property, Class<T> type) {
        return create(cdb.findObjById(parentId).getInstance(), property, type);
    }

    @Override
    public <T> T checkout(T obj) {
        ObjectMeta objectMeta = cdb.find(obj);
        snapshots.put(objectMeta.getId(), objectMeta.snapshot(PropertyFilter.CONVERTIBLE_TO_STRING));
        return obj;
    }

    @Override
    public <T> T checkout(Long id) {
        ObjectMeta<T> objectMeta = cdb.findObjById(id);
        snapshots.put(objectMeta.getId(), objectMeta.snapshot(PropertyFilter.CONVERTIBLE_TO_STRING));
        return objectMeta.getInstance();
    }

    @Override
    public void commit(Object... itemsToCommit) {
        for (Object obj : itemsToCommit) {
            ObjectMeta<?> objectMeta = cdb.find(obj);
            Map<String, Object> previous = snapshots.remove(objectMeta.getId());
            Map<String, Object> snapshot = objectMeta.snapshot(PropertyFilter.CONVERTIBLE_TO_STRING);
            List<Property> properties = objectMeta.getProperties();
            List<PropChange> changes = new ArrayList<PropChange>();
            for (Property property : properties) {
                Object oldVal = previous.get(property.getName());
                Object newVal = snapshot.get(property.getName());
                if(!Property.objEquals(oldVal, newVal)) {
                    changes.add(new PropChange(property.getName(),
                            property.convert(objectMeta, oldVal),
                            property.convert(objectMeta, newVal)));
                    //replace old value
                    property.set(obj, oldVal);
                }
            }
            if (!changes.isEmpty()) {
                List<PropChangeSet> changeSets = new ArrayList<PropChangeSet>();
                changeSets.add(new PropChangeSet(objectMeta.getId(), changes));
                updateObject(userId(), changeSets);
            }
        }
    }

    @Override
    public void moveInList(Object parent, Object objectToMove, int delta) {
        moveInList(parent, null, objectToMove, delta);
    }
    @Override
    public void moveInList(Object parent, String property, Object objectToMove, int delta) {
        Long id = id(objectToMove);
        ObjLoc objLoc = createObjLoc(parent, property, objectToMove.getClass(), PropertyFilter.LIST);
        moveInList(userId(), objLoc, id, delta);
    }

    @Override
    public void addRefs(Object parent, Object... objectsToAdd) {
        addRefs(parent, null, objectsToAdd);
    }

    @Override
    public void addRefs(Object parent, String property, Object... objectsToAdd) {
        ObjLoc objLoc = createObjLoc(parent, property, objectsToAdd[0].getClass(), PropertyFilter.REFERENCE);
        updateRefs(userId(), objLoc, toIds(objectsToAdd), new ArrayList<Long>());
    }

    @Override
    public void removeRefs(Object parent, Object... objectsToRemove) {
        removeRefs(parent, null, objectsToRemove);
    }

    @Override
    public void removeRefs(Object parent, String property, Object... objectsToRemove) {
        ObjLoc objLoc = createObjLoc(parent, property, objectsToRemove[0].getClass(), PropertyFilter.REFERENCE);
        updateRefs(userId(), objLoc, new ArrayList<Long>(), toIds(objectsToRemove));
    }

    public void changeType(Object obj, Class newType) {
        changeType(userId(), id(obj), newType);
    }

    @Override
    public <T> void cancelCheckout(T obj) {
        snapshots.remove(id((T) obj));
    }

    @Override
    public <T> void delete(T obj) {
        deleteObject(userId(), id(obj));
    }

    private <T> Long id(T obj) {
        return cdb.find(obj).getId();
    }

    @Override
    public <T> T getModel() {
        return (T) cdb.getRootInstance();
    }

    @Override
    public void moveTo(Object parent, Object objectToMove) {
        moveTo(parent, null, objectToMove);
    }

    @Override
    public void moveTo(Object parent, String property, Object objectToMove) {
        moveObject(userId(), id(objectToMove), createObjLoc(parent, property, objectToMove.getClass(), PropertyFilter.COMPLEX_NON_REFERENCE));
    }

    @Override
    public <T> T handleMessage(InMessage<ObjUpdate, T> inMessage) {
        inMessage.visit(remoteServer);
        syncSignal = new CountDownLatch(inMessage instanceof ChangeType ? 2 : 1); //we wait for 2 responses with change type
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

    private ObjLoc createObjLoc(Object parent, String property, Class objType, Filter<Property> filter) {
        ObjectMeta parentMeta = cdb.find(parent);
        if (property == null) {
            property = parentMeta.findMatchingProperty(objType, filter);
        }
        return new ObjLoc(id(parent), property, -1);
    }

    private List<Long> toIds(Object[] objectsToAdd) {
        List<Long> result = new ArrayList<Long>();
        for (Object o : objectsToAdd) {
            result.add(id(o));
        }
        return result;
    }
}
