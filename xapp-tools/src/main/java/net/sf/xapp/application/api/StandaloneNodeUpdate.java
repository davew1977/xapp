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
            node.refresh();
            appContainer.getApplication().nodeUpdated(node, changes);
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
            Node node = appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
            appContainer.getApplication().nodeAdded(node);
        }
    }

    @Override
    public ObjectMeta createObject(ObjectLocation homeLocation, ClassModel type) {
        ObjectMeta objMeta = type.newInstance(homeLocation, false);
        appContainer.getApplication().nodeAboutToBeAdded(homeLocation, objMeta);
        return objMeta;
    }

    @Override
    public void moveObject(ObjectLocation newLocation, ObjectMeta objectMeta) {
        moveObject(toNode(newLocation), objectMeta);
    }

    @Override
    public void moveObject(Node parentNode, Object obj) {
        moveObject(parentNode, getClassModel(obj).find(obj));
    }

    private void moveObject(Node parentNode, ObjectMeta objectMeta) {
        ObjectLocation newLoc = parentNode.toObjLocation();
        assert !newLoc.containsReferences();
        //find the old object and remove it
        objectMeta.setHome(newLoc, true);
        Node node = (Node) objectMeta.getAttachment();
        if (node != null) {
            appContainer.getApplication().nodeAboutToBeRemoved(node, true);
            appContainer.removeNode(node);
        }
        //create new node
        Node newNode = appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(newNode);
    }

    private Node toNode(ObjectLocation objectLocation) {
        Node node = (Node) objectLocation.getObj().getAttachment();
        if (node != null) {
            node = node.find(objectLocation.getProperty());
        }
        return node;
    }

    @Override
    public void insertObject(Node parentNode, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).createObjMeta(parentNode.toObjLocation(), obj, true, -1);
        //create the node
        insertNode(parentNode, objectMeta);
    }

    private void insertNode(Node parentNode, ObjectMeta objectMeta) {
        Node newNode = appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(newNode);
        appContainer.getMainPanel().repaint();
    }

    @Override
    public void createReference(Node parentNode, Object obj) {
        ObjectMeta objMeta = getClassModel(obj).find(obj);
        objMeta.createAndSetReference(parentNode.toObjLocation());
        appContainer.getNodeBuilder().createNode(parentNode, objMeta);
    }

    @Override
    public void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove) {
        for (ObjectMeta objectMeta : refsToRemove) {
            objectMeta.removeAndUnsetReference(objectLocation);
        }
        for (ObjectMeta objectMeta : refsToAdd) {
            objectMeta.createAndSetReference(objectLocation);
        }
        Node node = toNode(objectLocation);
        appContainer.getNodeBuilder().refresh(node);
        Map<String, PropertyChange> map = new HashMap<String, PropertyChange>();
        ContainerProperty containerProperty = node.getListNodeContext().getContainerProperty();
        map.put(containerProperty.getName(), new RegularPropertyChange(containerProperty, node.wrappedObject(), refsToRemove, refsToAdd));
        node.getAppContainer().getApplication().nodeUpdated(node, map);
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
        ObjectMeta newInstance = targetClassModel.newInstance(objHome, true);
        List<Property> properties = targetClassModel.getAllProperties();
        for (Property property : properties) {
            newInstance.set(property, obj.get(property));
        }
        deleteObject(obj);

        objHome.setIndex(newInstance, oldIndex);
        //refresh so a new Node will be created, then we must select that node
        //so that the whole operation is more transparent to the user
        Node newNode = appContainer.getNodeBuilder().createNode(parent, newInstance, oldIndex);
        appContainer.setSelectedNode(newNode);
    }

    @Override
    public ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String text) {
        return deserializeAndInsert(toNode(objectLocation), classModel, text, Charset.forName("UTF-8")); //UTF 8 here, coz text comes from server
    }

    @Override
    public ObjectMeta deserializeAndInsert(Node node, ClassModel classModel, String text) {
        return deserializeAndInsert(node, classModel, text, Charset.defaultCharset()); //need default charset when pasting, coz then the text comes from the FS
    }

    public ObjectMeta deserializeAndInsert(Node node, ClassModel classModel, String text, Charset charset) {
        Unmarshaller un = new Unmarshaller(classModel);
        ObjectMeta objectMeta = un.unmarshalString(text, charset, node.toObjLocation());
        insertNode(node, objectMeta);
        return objectMeta;
    }

    @Override
    public void removeReference(Node node) {
        assert node.isReference();
        node.objectMeta().removeAndUnsetReference(node.myObjLocation());
        appContainer.getApplication().nodeAboutToBeRemoved(node, false);
        appContainer.removeNode(node);

    }

    @Override
    public void moveInList(Node node, int delta) {
        //update model
        int newIndex = node.objectMeta().updateIndex(node.getParent().toObjLocation(), delta);

        //update jtree
        node.updateIndex(newIndex);
    }

    @Override
    public void deleteObject(ObjectMeta objectMeta) {
        //remove from data model
        Collection<Node> attachments = (Collection<Node>) objectMeta.dispose();
        //clean up nodes
        Node node = (Node) objectMeta.getAttachment();
        if (node != null) {
            appContainer.getApplication().nodeAboutToBeRemoved(node, false);
            appContainer.removeNode(node);
        }

        for (Node referencingNode : attachments) {
            appContainer.getApplication().nodeAboutToBeRemoved(referencingNode, false);
            appContainer.removeNode(referencingNode);
        }
    }

    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }
}
