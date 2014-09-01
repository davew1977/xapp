package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.ObjRef;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class NodeUpdateApiRemote implements NodeUpdateApi {
    private ApplicationContainer appContainer;
    private ObjUpdate remote;

    public NodeUpdateApiRemote(ApplicationContainer appContainer, ObjUpdate remote) {
        this.appContainer = appContainer;
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
    public void moveObject(Node parentNode, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).find(obj);
        remote.moveObject(objectMeta.getId(), toObjLoc(parentNode));
    }

    @Override
    public void insertObject(Node parentNode, Object obj) {
        String xml = appContainer.getClassDatabase().createMarshaller(obj.getClass()).toXMLString(obj);
        remote.createObject(toObjLoc(parentNode), obj.getClass(), xml);
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
    public void createReference(Node parentNode, Object obj) {
        remote.createRefs();
    }

    @Override
    public void removeReference(Node referenceNode) {

    }

    @Override
    public void moveInList(Node node, int delta) {

    }

    @Override
    public void updateReferences(Node node, List<Object> newValues) {
        Collection oldValues = node.getListNodeContext().getCollection();   //TODO repeated code here with standalone update api
        List<Object> toRemove = new ArrayList<Object>(oldValues);
        List<Object> toAdd = new ArrayList<Object>(newValues);
        toRemove.removeAll(newValues);
        toAdd.removeAll(oldValues);
        //remove unlinked references
        for (Object oldValue : toRemove) {
            ObjectMeta objMeta = getClassModel(oldValue).find(oldValue);
        }
        //add new references
        for (Object newValue : toAdd) {

            ObjectMeta objMeta = getClassModel(newValue).find(newValue);
        }


    }

    private void createRefs(ObjectLocation objectLocation, List<ObjectMeta> objMetas) {
        remote.createRefs(toRefs(objectLocation, objMetas));
    }

    private void deleteRefs(ObjectLocation objectLocation, List<ObjectMeta> objMetas) {
        remote.deleteRefs(toRefs(objectLocation, objMetas));
    }

    @Override
    public Node changeType(ObjectMeta obj, ClassModel targetClassModel) {
        return null;
    }

    @Override
    public ObjectMeta deserializeAndInsert(Node node, ClassModel classModel, String text) {

        return null;
    }

    @Override
    public void moveObject(ObjectLocation objectLocation, ObjectMeta obj) {

    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String text) {
        throw new UnsupportedOperationException("should not be called in this context");
    }

    private ObjLoc toObjLoc(Node node) {
        return toObjLoc(node.toObjLocation(), -1);
    }

    private ObjLoc toObjLoc(ObjectLocation objectLocation) {
        return toObjLoc(objectLocation, -1);
    }
    private ObjLoc toObjLoc(ObjectLocation objectLocation, int index) {
        return new ObjLoc(objectLocation.getObj().getId(), objectLocation.getProperty().getName(), index);
    }
    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }

    private List<ObjRef> toRefs(ObjectLocation objectLocation, List<ObjectMeta> objMetas) {
        List<ObjRef> refs = new ArrayList<ObjRef>();
        for (ObjectMeta objMeta : objMetas) {
            refs.add(new ObjRef(objMeta.getId(), toObjLoc(objectLocation)));
        }
        return refs;
    }
}
