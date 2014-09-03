package net.sf.xapp.objclient;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class NodeUpdateApiRemote implements NodeUpdateApi {
    private ClassDatabase cdb;
    private ObjUpdate remote;

    public NodeUpdateApiRemote(ClassDatabase cdb, ObjUpdate remote) {
        this.cdb = cdb;
        this.remote = remote;
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
        remote.updateObject(changeSets);
    }

    @Override
    public ObjectMeta createObject(ObjectLocation homeLocation, ClassModel type) {
        //TODO introduce synchronous service on server to handle object creation
        //TODO typically to handle default values etc
        return type.newInstance(homeLocation, false);
    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        initObject(objectMeta, potentialUpdates);
    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        objectMeta.update(potentialUpdates);
        remote.createObject(toObjLoc(objectMeta.getHome()), objectMeta.getType(), objectMeta.toXml());
        //delete local version of object
        objectMeta.dispose();
        return null;
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {
        remote.deleteObject(objectMeta.getId());
    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int delta) {
        remote.moveInList(toObjLoc(objectLocation), objectMeta.getId(), delta);
    }

    @Override
    public void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove) {
        remote.updateRefs(toObjLoc(objectLocation), toIds(refsToAdd), toIds(refsToRemove));
    }

    @Override
    public void changeType(ObjectMeta obj, ClassModel targetClassModel) {
        remote.changeType(obj.getId(), targetClassModel.getContainedClass());
    }

    @Override
    public void insertObject(ObjectLocation objectLocation, Object obj) {
        String xml = cdb.createMarshaller(obj.getClass()).toXMLString(obj);
        remote.createObject(toObjLoc(objectLocation), obj.getClass(), xml);
    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String xml, Charset charset) {
        remote.createObject(toObjLoc(objectLocation), classModel.getContainedClass(), xml);
        return null;
    }

    @Override
    public void moveOrInsertObjMeta(ObjectLocation objectLocation, ObjectMeta objMeta) {
        remote.moveObject(objMeta.getId(), toObjLoc(objectLocation));
    }

    private ObjLoc toObjLoc(ObjectLocation objectLocation) {
        return new ObjLoc(objectLocation.getObj().getId(), objectLocation.getProperty().getName());
    }

    private List<Long> toIds(List<ObjectMeta> refsToAdd) {
        List<Long> result = new ArrayList<Long>();
        for (ObjectMeta objectMeta : refsToAdd) {
            result.add(objectMeta.getId());
        }
        return result;
    }
}
