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
    public ObjectMeta createObject(Node parentNode, ClassModel type) {
        return null;
    }

    @Override
    public void moveObject(Node parentNode, Object obj) {

    }

    @Override
    public void insertObject(Node parentNode, Object obj) {

    }

    @Override
    public void initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

    }

    @Override
    public void deleteObject(ObjectMeta objMeta) {

    }

    @Override
    public void createReference(Node parentNode, Object obj) {

    }

    @Override
    public void removeReference(Node parentNode, ObjectMeta objectMeta) {

    }

    @Override
    public void moveInList(Node parentNode, ObjectMeta objectMeta, int newIndex) {

    }
}
