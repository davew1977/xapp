package net.sf.xapp.application.api;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.core.*;

import java.nio.charset.Charset;
import java.util.*;

/**
 */
public class StandaloneNodeUpdate implements NodeUpdateApi {
    private final ApplicationContainer appContainer;

    public StandaloneNodeUpdate(ApplicationContainer appContainer) {
        this.appContainer = appContainer;
    }

    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

        Map<String, PropertyChange> changes = objectMeta.update(potentialUpdates);
        Node node = (Node) objectMeta.getAttachment();
        if (node != null) { //todo only refresh if sub-objects have changed
            Node newNode = node.refresh();
            appContainer.getApplication().nodeUpdated(node, changes);
            appContainer.expand(newNode);
        }
    }

    @Override
    public void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates) {
        for (ObjectMeta objectMeta : objectMetas) {
            updateObject(objectMeta, potentialUpdates);
        }
    }

    @Override
    public PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        objectMeta.update(potentialUpdates);
        PropertyChange propertyChange = objectMeta.setHomeRef();
        //try call post init on the parent object //todo this is a trial to see if it is good
        objectMeta.getParent().postInit();
        return propertyChange;
    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        PropertyChange propertyChange = initObject(objectMeta, potentialUpdates);
        if (propertyChange.succeeded()) { //could fail if we're added an identical object to a set
            createNode(parentNode, objectMeta);
        }
    }

    @Override
    public void createObject(ObjectLocation homeLocation, ClassModel type, ObjCreateCallback callback) {
        ObjectMeta objMeta = type.newInstance(homeLocation, false);
        appContainer.getApplication().nodeAboutToBeAdded(homeLocation, objMeta);
        callback.objCreated(objMeta);
    }

    public void moveOrInsertObjMeta(ObjectLocation newLoc, ObjectMeta objMeta) {
        assert !newLoc.containsReferences();
        //find the old object and remove it
        objMeta.setHome(newLoc, true);
        Node node = (Node) objMeta.getAttachment();
        if (node != null) {
            appContainer.getApplication().nodeAboutToBeRemoved(node, true);
            appContainer.removeNode(node);
        }
        //create new node
        createNode(toNode(newLoc), objMeta);
    }

    @Override
    public void insertObject(ObjectLocation objectLocation, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).createObjMeta(objectLocation, obj, true);
        //create the node
        createNode(toNode(objectLocation), objectMeta);
    }

    @Override
    public void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove) {
        Node parentNode = toNode(objectLocation);
        for (ObjectMeta objectMeta : refsToRemove) {
            Node node = (Node) objectMeta.removeAndUnsetReference(objectLocation);
            removeNode(node);
        }
        for (ObjectMeta objectMeta : refsToAdd) {
            objectMeta.createAndSetReference(objectLocation);
            createNode(parentNode, objectMeta);
        }
    }

    @Override
    public void changeType(ObjectMeta obj, ClassModel targetClassModel) {
        if (obj.hasReferences()) {
            /*
            to implement this we need to delete the references(done) and reset them where appropriate (not done)
             */
            throw new UnsupportedOperationException("cannot currently change type on an object which has references");
        }
        Node node = (Node) obj.getAttachment();
        Node parent = node.getParent();
        ObjectLocation objHome = obj.getHome();
        int oldIndex = node.index();
        deleteObject(obj);
        ObjectMeta newInstance = targetClassModel.newInstance(objHome, true);
        newInstance.setId(obj.getId());
        List<Property> properties = targetClassModel.getAllProperties();
        for (Property property : properties) {
            newInstance.set(property, obj.get(property));
        }

        objHome.setIndex(newInstance, oldIndex);
        //refresh so a new Node will be created, then we must select that node
        //so that the whole operation is more transparent to the user
        Node newNode = appContainer.getNodeBuilder().createNode(parent, newInstance, oldIndex);
        appContainer.setSelectedNode(newNode);
    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String xml, Charset charset) {
        Unmarshaller un = new Unmarshaller(classModel);
        ObjectMeta objectMeta = un.unmarshalString(xml, charset, objectLocation);
        createNode(toNode(objectLocation), objectMeta);
        return objectMeta;
    }

    @Override
    public void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int delta) {
        int newIndex = objectMeta.updateIndex(objectLocation, delta);
        ((Node) objectMeta.getAttachment(objectLocation)).updateIndex(newIndex);
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {
        //remove from data model
        Collection<Node> attachments = (Collection<Node>) objectMeta.dispose();
        //clean up nodes
        Node node = (Node) objectMeta.getAttachment();
        if (node != null) {
            removeNode(node);
        }

        for (Node referencingNode : attachments) {
            removeNode(referencingNode);
        }
    }

    private void removeNode(Node node) {
        if (node != null) {
            appContainer.getApplication().nodeAboutToBeRemoved(node, false);
            appContainer.removeNode(node);
        }
    }

    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }

    private Node toNode(ObjectLocation objectLocation) {
        Node node = (Node) objectLocation.getObj().getAttachment();
        if (node != null) {
            node = node.find(objectLocation.getProperty());
        }
        return node;
    }

    private Node createNode(Node parentNode, ObjectMeta objectMeta) {
        Node newNode = appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(newNode);
        appContainer.getMainPanel().repaint();
        return newNode;
    }
}
