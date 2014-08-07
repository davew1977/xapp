package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

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
        if(node != null) { //todo only refresh if sub-objects have changed
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
        return objectMeta.setHomeRef();
    }

    @Override
    public void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {
        PropertyChange propertyChange = initObject(objectMeta, potentialUpdates);
        if(propertyChange.succeeded()) { //could fail if we're added an identical object to a set
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
    public void moveObject(Node parentNode, Object obj) {
        ObjectLocation newLoc = parentNode.toObjLocation();
        assert !newLoc.containsReferences();
        //find the old object and remove it
        ObjectMeta objectMeta = getClassModel(obj).find(obj);
        objectMeta.setHome(newLoc, true);
        Node node = (Node) objectMeta.getAttachment();
        if (node!=null) {
            appContainer.getApplication().nodeAboutToBeRemoved(node, true);
            appContainer.removeNode(node);
        }
        //create new node
        Node newNode = appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(newNode);
    }

    @Override
    public void insertObject(Node parentNode, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).createObjMeta(parentNode.toObjLocation(), obj, true, -1);
        //create the node
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
    public void updateReferences(Node node, List<Object> newValues) {
        Collection oldValues = node.getListNodeContext().getCollection();
        List<Object> toRemove = new ArrayList<Object>(oldValues);
        List<Object> toAdd = new ArrayList<Object>(newValues);
        toRemove.removeAll(newValues);
        toAdd.removeAll(oldValues);
        //remove unlinked references
        for (Object oldValue : toRemove) {
            ObjectMeta objMeta = getClassModel(oldValue).find(oldValue);
            objMeta.removeAndUnsetReference(node.toObjLocation());
        }
        //add new references
        for (Object newValue : toAdd) {

            ObjectMeta objMeta = getClassModel(newValue).find(newValue);
            objMeta.createAndSetReference(node.toObjLocation());
        }

        boolean unchanged = toAdd.isEmpty() && toRemove.isEmpty();

        if(!unchanged) {
            appContainer.getNodeBuilder().refresh(node);
            Map<String, PropertyChange> map = new HashMap<String, PropertyChange>();
            ContainerProperty containerProperty = node.getListNodeContext().getContainerProperty();
            map.put(containerProperty.getName(), new RegularPropertyChange(containerProperty, node.wrappedObject(), oldValues, newValues));
            node.getAppContainer().getApplication().nodeUpdated(node, map);
        }
    }

    @Override
    public Node changeType(ObjectMeta obj, ClassModel targetClassModel) {
        if(obj.hasReferences()) {
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
        for (Property property : properties)
        {
            newInstance.set(property, obj.get(property));
        }
        deleteObject(obj);

        objHome.setIndex(newInstance, oldIndex);
        //refresh so a new Node will be created, then we must select that node
        //so that the whole operation is more transparent to the user
        return appContainer.getNodeBuilder().createNode(parent, newInstance, oldIndex);
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

        for (Node referencingNode: attachments) {
            appContainer.getApplication().nodeAboutToBeRemoved(referencingNode, false);
            appContainer.removeNode(referencingNode);
        }
    }

    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }
}
