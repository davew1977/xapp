package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

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
    public void moveObject(ObjectMeta objectMeta, int oldIndex, int newIndex) {

    }

    @Override
    public void addNode(Node node) {

    }

    @Override
    public void removeNode(Node node) {

    }

    @Override
    public void moveNode(Node node, int oldIndex, int newIndex) {

    }

    @Override
    public ObjectMeta createObject(ObjectLocation location, ClassModel type) {
        return null;
    }

    @Override
    public void moveObject(ObjectLocation newLocation, Object obj) {

    }

    @Override
    public void insertObject(ObjectLocation newLocation, Object obj) {

    }

    @Override
    public void initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void deleteObject(ObjectMeta objMeta) {

    }

    @Override
    public void createReference(ObjectLocation objectLocation, Object obj) {

    }

    @Override
    public void removeReference(ObjectLocation objectLocation, ObjectMeta objectMeta) {

    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int newIndex) {

    }
}
