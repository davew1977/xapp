package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class StandaloneNodeUpdate implements NodeUpdateApi {
    private final ApplicationContainer appContainer;

    public StandaloneNodeUpdate(ApplicationContainer appContainer) {
        this.appContainer = appContainer;
    }

    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        //find node
        Node node = (Node) objectMeta.getAttachment();
        if(node != null) {
            node.refresh();
        }
        appContainer.getApplication().objectUpdated(objectMeta, PropertyUpdate.execute(objectMeta, potentialUpdates));
    }

    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {
        for (ObjectMeta objectMeta : objectMetas) {
            updateObject(objectMeta, potentialUpdates);
        }
    }

    @Override
    public void initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        objectMeta.update(potentialUpdates);
        objectMeta.setHomeRef();
    }

    @Override
    public void addObject(ObjectMeta objectMeta) {

    }

    @Override
    public void removeObject(ObjectMeta objectMeta) {

    }

    @Override
    public void moveObject(ObjectMeta objectMeta, int oldIndex, int newIndex) {

    }

    @Override
    public void addObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        PropertyUpdate.execute(objectMeta, potentialUpdates);
        addObject(parentNode, objectMeta);
    }

    @Override
    public void addObject(Node parentNode, ObjectMeta objectMeta) {
        parentNode.add(objectMeta);
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
    public ObjectMeta createObject(ObjectMeta parent, Property property, ClassModel type) {
        final ObjectMeta objMeta = type.newInstance(parent, property);
        appContainer.getApplication().nodeAboutToBeAdded(objMeta);
        return objMeta;
    }

    @Override
    public ObjectMeta registerObject(ObjectMeta parent, Property property, ClassModel type, Object obj) {
        //todo try to find the obj, and if it is found, we need to remove the old node

        final ObjectMeta objMeta = type.findOrCreate(parent, property, obj);
        objMeta.setHomeRef();
        appContainer.getApplication().nodeAdded(objMeta);
        return objMeta;
    }

    @Override
    public void createReference(ObjectMeta parent, Property property, ClassModel type, Object obj) {
        ObjectMeta objMeta = type.find(obj);
        objMeta.createAndSetReference(parent, property);
    }

    @Override
    public void deleteObject(ObjectMeta objMeta) {
        objMeta.dispose();
    }
}
