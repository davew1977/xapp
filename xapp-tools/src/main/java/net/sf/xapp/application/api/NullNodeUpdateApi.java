package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class NullNodeUpdateApi implements NodeUpdateApi {
    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {

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

    @Override
    public ObjectMeta createObject(ObjectLocation homeLocation, ClassModel type) {
        return null;
    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        return null;
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {

    }

    @Override
    public void moveObject(Node parentNode, Object obj) {

    }

    @Override
    public void insertObject(Node parentNode, Object obj) {

    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

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
}
