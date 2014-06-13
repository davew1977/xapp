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
        objectMeta.update(potentialUpdates);
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
    public ObjectMeta createObject(ObjectLocation location, ClassModel type) {
        final ObjectMeta objMeta = type.newInstance(location);
        appContainer.getApplication().nodeAboutToBeAdded(objMeta);
        return objMeta;
    }

    @Override
    public void moveObject(ObjectLocation newLocation, ClassModel type, Object obj) {
        //find the old object and remove it
        ObjectMeta objectMeta = type.find(obj);
        deleteObject(objectMeta, true);
        //todo create the node
        appContainer.getApplication().nodeAdded(objectMeta);
    }

    @Override
    public void insertObject(ObjectLocation newLocation, ClassModel type, Object obj) {
        ObjectMeta objectMeta = type.registerInstance(newLocation, obj);
        //todo create the node

        appContainer.getApplication().nodeAdded(objectMeta);

    }

    @Override
    public void createReference(ObjectLocation objectLocation, ClassModel type, Object obj) {
        ObjectMeta objMeta = type.find(obj);
        objMeta.createAndSetReference(objectLocation);
    }

    @Override
    public void deleteObject(ObjectMeta objMeta) {
        deleteObject(objMeta, false);

    }

    private void deleteObject(ObjectMeta objMeta, boolean wasMoved) {
        //remove from data model
        objMeta.dispose();
        //clean up nodes
        Node node = (Node) objMeta.getAttachment();
        if (node!=null) {
            appContainer.removeNode(node, true);
        }
        List<Node> referencingNodes = appContainer.findReferencingNodes(objMeta.getInstance());
        for (Node referencingNode : referencingNodes) {
            appContainer.removeNode(referencingNode, wasMoved);
        }
    }
}
