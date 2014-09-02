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
        ClassModel cm = appContainer.getClassDatabase().getClassModel(obj.getClass());
        createRefs(parentNode.toObjLocation(), Arrays.asList(cm.find(obj)));
    }

    @Override
    public void moveInList(Node node, int delta) {
        remote.moveInList(toRef(node), delta);
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
    public ObjectMeta deserializeAndInsert(Node node, ClassModel classModel, String xml) {
        remote.createObject(toObjLoc(node), classModel.getContainedClass(), xml);
        return null;
    }

    @Override
    public void moveObject(ObjectLocation objectLocation, ObjectMeta obj) {
        remote.moveObject(obj.getId(), toObjLoc(objectLocation));
    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String text) {
        throw new UnsupportedOperationException("should not be called in this context");
    }

    private ObjLoc toObjLoc(Node node) {
        return toObjLoc(node.toObjLocation());
    }

    private ObjLoc toObjLoc(ObjectLocation objectLocation) {
        return new ObjLoc(objectLocation.getObj().getId(), objectLocation.getProperty().getName());
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

    private ObjRef toRef(Node node) {
        return new ObjRef(node.objectMeta().getId(), toObjLoc(node));
    }
    private List<Long> toIds(List<ObjectMeta> refsToAdd) {
        List<Long> result = new ArrayList<Long>();
        for (ObjectMeta objectMeta : refsToAdd) {
            result.add(objectMeta.getId());
        }
        return result;
    }
}
