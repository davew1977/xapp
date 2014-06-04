package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.ClassModel;
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
    public void addObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void addObject(Node parentNode, ObjectMeta objectMeta) {

    }

    @Override
    public void removeObject(ObjectMeta objectMeta) {

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
    public ObjectMeta createObject(Node parentNode, ClassModel type) {
        return null;
    }

    @Override
    public ObjectMeta registerObject(Node parentNode, ClassModel type, Object obj) {
        return null;
    }

    @Override
    public void cancelObject(ObjectMeta objMeta) {

    }
}
