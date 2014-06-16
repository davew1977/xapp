package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.util.List;

import static net.sf.xapp.application.api.ObjectNodeContext.ObjectContext.*;

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
        //create the node
        ObjectLocation objectLocation = objectMeta.getHome();
        appContainer.getNodeBuilder().createNode(objectLocation.getProperty(), objectMeta,
                (Node) objectLocation.getAttachment(), objectLocation.isCollection() ? IN_LIST : PROPERTY);
        appContainer.getApplication().nodeAdded(objectMeta);
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
        final ObjectMeta objMeta = type.newInstance(location);
        appContainer.getApplication().nodeAboutToBeAdded(objMeta);
        return objMeta;
    }

    @Override
    public void moveObject(ObjectLocation newLocation, Object obj) {
        assert !newLocation.containsReferences();
        //find the old object and remove it
        ObjectMeta objectMeta = getClassModel(obj).find(obj);
        ObjectLocation oldLocation = objectMeta.setHome(newLocation);
        Node node = (Node) objectMeta.getAttachment();
        if (node!=null) {
            appContainer.removeNode(node, true);
        }
        //create new node
        appContainer.getNodeBuilder().createNode(newLocation, objectMeta, IN_LIST);
        appContainer.getApplication().nodeAdded(objectMeta);
    }

    @Override
    public void insertObject(ObjectLocation newLocation, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).registerInstance(newLocation, obj);
        //create the node
        appContainer.getNodeBuilder().createNode(newLocation.getProperty(), objectMeta,
                (Node) newLocation.getAttachment(), IN_LIST, newLocation.index());
        appContainer.getApplication().nodeAdded(objectMeta);

    }

    @Override
    public void createReference(ObjectLocation newLocation, Object obj) {
        ObjectMeta objMeta = getClassModel(obj).find(obj);
        objMeta.createAndSetReference(newLocation);
        appContainer.getNodeBuilder().createNode(newLocation, objMeta, IN_LIST);
    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int newIndex) {
        //update model
        objectMeta.updateIndex(objectLocation, newIndex);
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

    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }
}
