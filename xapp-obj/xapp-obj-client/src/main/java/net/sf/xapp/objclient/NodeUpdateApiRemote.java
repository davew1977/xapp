package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.XmlObj;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class NodeUpdateApiRemote implements NodeUpdateApi {
    private ApplicationContainer appContainer;
    private ObjListener remote;
    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public ObjectMeta createObject(ObjectLocation homeLocation, ClassModel type) {
        return null;
    }

    @Override
    public void moveObject(Node parentNode, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).find(obj);
        remote.objMoved(objectMeta.getId(), toObjLoc(parentNode));
    }

    @Override
    public void insertObject(Node parentNode, Object obj) {
        String xml = appContainer.getClassDatabase().createMarshaller(obj.getClass()).toXMLString(obj);
        remote.objAdded(toObjLoc(parentNode), new XmlObj(xml, -1L, -1L));
    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        return null;
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {

    }

    @Override
    public void createReference(Node parentNode, Object obj) {

    }

    @Override
    public void removeReference(Node referenceNode) {

    }

    @Override
    public void moveInList(Node node, int delta) {

    }

    @Override
    public void updateReferences(Node node, List<Object> objects) {

    }

    @Override
    public Node changeType(ObjectMeta obj, ClassModel targetClassModel) {
        return null;
    }

    @Override
    public void deserializeAndInsert(Node node, ClassModel classModel, String text) {

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
}
