package net.sf.xapp.objclient;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.api.ObjCreateCallback;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class NodeUpdateApiRemote implements NodeUpdateApi {
    private ObjClient objClient;
    private ObjCreateCallback objCreateCallback;

    public NodeUpdateApiRemote(final ClassDatabase cdb, ObjClient objClient) {
        this.objClient = objClient;
        objClient.addObjListener(new ObjListenerAdaptor() {
            @Override
            public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
                if (userId().equals(user) && objCreateCallback != null) { //obj create callback can be null if we are replaying offline changes
                    objCreateCallback.objCreated(cdb.findObjById(obj.getId()));
                    objCreateCallback = null;
                }
            }
        });
    }

    private void setObjCreateCallback(ObjCreateCallback objCreateCallback) {
        if(this.objCreateCallback != null) {
            throw new IllegalArgumentException("One create at a time!");
        }
        this.objCreateCallback = objCreateCallback;
    }

    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        updateObjects(Arrays.asList(objectMeta), potentialUpdates);
    }
    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {
        List<PropChangeSet> changeSets = new ArrayList<PropChangeSet>();

        for (ObjectMeta objectMeta : objectMetas) {
            List<PropChange> changes = new ArrayList<PropChange>();
            for (PropertyUpdate potentialUpdate : potentialUpdates) {
                String prop = potentialUpdate.getPropertyName();
                String oldVal = potentialUpdate.oldValAsString(objectMeta);
                String newVal = potentialUpdate.newValAsString(objectMeta);
                changes.add(new PropChange(prop, oldVal, newVal));
            }
            changeSets.add(new PropChangeSet(objectMeta.getId(), changes));
        }
        remote().updateObject(userId(), changeSets);
    }

    @Override
    public void createObject(ObjectLocation homeLocation, ClassModel type, ObjCreateCallback callback) {
        setObjCreateCallback(callback);
        remote().createEmptyObject(userId(), toObjLoc(homeLocation), type.getContainedClass());
    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        initObject(objectMeta, potentialUpdates);
    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        updateObject(objectMeta, potentialUpdates);
        return null;
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {
        remote().deleteObject(userId(), toObjLoc(objectMeta.getHome()), objectMeta.getId());
    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int delta) {
        remote().setIndex(userId(), toObjLoc(objectLocation), objectMeta.getId(), objectMeta.index(objectLocation) + delta);
    }

    @Override
    public void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove) {
        remote().updateRefs(userId(), toObjLoc(objectLocation), toIds(refsToAdd), toIds(refsToRemove));
    }

    @Override
    public void changeType(ObjectMeta obj, ClassModel targetClassModel) {
        remote().changeType(userId(), toObjLoc(obj.getHome()), obj.getId(), targetClassModel.getContainedClass());
    }

    @Override
    public void insertObject(ObjectLocation objectLocation, Object obj) {
        String xml = objClient.getCdb().createMarshaller(obj.getClass()).toXMLString(obj);
        remote().createObject(userId(), toObjLoc(objectLocation), obj.getClass(), xml);
    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String xml, Charset charset) {
        remote().createObject(userId(), toObjLoc(objectLocation), classModel.getContainedClass(), xml);
        return null;
    }

    @Override
    public void moveOrInsertObjMeta(ObjectLocation objectLocation, ObjectMeta objMeta) {
        remote().moveObject(userId(), objMeta.getId(), toObjLoc(objMeta.getHome()), toObjLoc(objectLocation));
    }

    public static ObjLoc toObjLoc(ObjectLocation objectLocation) {
        return new ObjLoc(objectLocation.getObj().getId(), objectLocation.getProperty().getName(), objectLocation.getIndex());
    }

    private List<Long> toIds(List<ObjectMeta> refsToAdd) {
        List<Long> result = new ArrayList<Long>();
        for (ObjectMeta objectMeta : refsToAdd) {
            result.add(objectMeta.getId());
        }
        return result;
    }

    private UserId userId() {
        return objClient.getUserId();
    }
    private ObjUpdate remote() {
        return objClient.getObjUpdate();
    }
}
