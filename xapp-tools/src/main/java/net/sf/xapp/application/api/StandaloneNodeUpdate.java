package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.util.Collection;
import java.util.List;

/**
 */
public class StandaloneNodeUpdate implements NodeUpdateApi {
    private final ApplicationContainer appContainer;

    public StandaloneNodeUpdate(ApplicationContainer appContainer) {
        this.appContainer = appContainer;
    }

    @Override
    public void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates) {

        objectMeta.update(potentialUpdates);
        appContainer.getApplication().objectUpdated(objectMeta, PropertyUpdate.execute(objectMeta, potentialUpdates)); //find node
        Node node = (Node) objectMeta.getAttachment();
        if(node != null) { //todo only refresh if sub-objects have changed
            node.refresh();
        }
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
        Node node = (Node) objectMeta.getAttachment();
        if(node != null) { //todo only refresh if sub-objects have changed
            node.refresh();
        }
        appContainer.getApplication().nodeAdded(objectMeta);
    }

    @Override
    public ObjectMeta createObject(Node parentNode, ClassModel type) {
        final ObjectMeta objMeta = type.newInstance(parentNode.toObjLocation());
        appContainer.getApplication().nodeAboutToBeAdded(objMeta);

        //create the node (may be removed if user cancels)
        appContainer.getNodeBuilder().createNode(parentNode, objMeta);

        return objMeta;
    }

    @Override
    public void moveObject(Node parentNode, Object obj) {
        ObjectLocation newLoc = parentNode.toObjLocation();
        assert !newLoc.containsReferences();
        //find the old object and remove it
        ObjectMeta objectMeta = getClassModel(obj).find(obj);
        objectMeta.setHome(newLoc);
        Node node = (Node) objectMeta.getAttachment();
        if (node!=null) {
            appContainer.removeNode(node);
            appContainer.getApplication().nodeRemoved(node, true);
        }
        //create new node
        appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(objectMeta);
    }

    @Override
    public void insertObject(Node parentNode, Object obj) {
        ObjectMeta objectMeta = getClassModel(obj).registerInstance(parentNode.toObjLocation(), obj);
        //create the node
        appContainer.getNodeBuilder().createNode(parentNode, objectMeta);
        appContainer.getApplication().nodeAdded(objectMeta);
        appContainer.getMainPanel().repaint();
    }

    @Override
    public void createReference(Node parentNode, Object obj) {
        ObjectMeta objMeta = getClassModel(obj).find(obj);
        objMeta.createAndSetReference(parentNode.toObjLocation());
        appContainer.getNodeBuilder().createNode(parentNode, objMeta);
    }

    @Override
    public void removeReference(Node node) {
        assert node.isReference();
        node.objectMeta().removeAndUnsetReference(node.myObjLocation());
        appContainer.removeNode(node);
        appContainer.getApplication().nodeRemoved(node, false);

    }

    @Override
    public void moveInList(Node parentNode, ObjectMeta objectMeta, int newIndex) {
        //update model
        objectMeta.updateIndex(parentNode.toObjLocation(), newIndex);

        //update jtree
        Node node = (Node) objectMeta.getAttachment();
        node.updateIndex(newIndex);
    }

    @Override
    public void deleteObject(Node node) {
        //remove from data model
        Collection<Node> attachments = (Collection<Node>) node.objectMeta().dispose();
        //clean up nodes
        appContainer.removeNode(node);
        appContainer.getApplication().nodeRemoved(node, true);

        for (Node referencingNode: attachments) {
            appContainer.removeNode(referencingNode);
            appContainer.getApplication().nodeRemoved(node, false);
        }
    }

    private ClassModel<Object> getClassModel(Object obj) {
        return appContainer.getClassDatabase().getClassModel(obj.getClass());
    }
}
