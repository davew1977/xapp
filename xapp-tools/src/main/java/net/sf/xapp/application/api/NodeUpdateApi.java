package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.util.List;

/**
 * acts as a bridge between the UI and the application controller. In classic XAPP, these notification simply feed
 * through to the client side application. In Xapp Cloud the notifications will be sent to the server, which will decide
 * when and how to update the clients
 */
public interface NodeUpdateApi {

    /**
     * called when the object has no node
     * @param objectMeta object that has changed
     * @param potentialUpdates updates
     */
    void updateObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);
    void updateObjects(List<ObjectMeta> objectMetas, List<PropertyUpdate> potentialUpdates);
    void moveObject(ObjectMeta objectMeta, int oldIndex, int newIndex);
    /**
     * index has a value if the containing property is a list (null if map or set)
     */
    void addNode(Node node);
    void removeNode(Node node);
    /**
     * Node was moved up or down in a list
     */
    void moveNode(Node node, int oldIndex, int newIndex);

    /**
     * creates the initial raw instance of the object
     * will be shortly followed with either an initialize or a cancel
     */
    ObjectMeta createObject(ObjectLocation location, ClassModel type);

    /**
     * This is used when an object already exists but has been moved within the object graph
     */
    void moveObject(ObjectLocation newLocation, ClassModel type, Object obj);
    void insertObject(ObjectLocation newLocation, ClassModel type, Object obj);

    /**
     * initialize a recently created object with properties entered by the user
     */
    void initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);

    /**
     * delete/rollback creation of a recently created object
     */
    void deleteObject(ObjectMeta objMeta);

    void createReference(ObjectLocation objectLocation, ClassModel classModel, Object obj);
    void removeReference(ObjectLocation objectLocation, ObjectMeta objectMeta);
}
