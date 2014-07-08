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
    /**
     * creates the initial raw instance of the object
     * will be shortly followed with either an initialize or a cancel
     *
     * created object is not yet placed in the model
     */
    ObjectMeta createObject(ObjectLocation homeLocation, ClassModel type);

    /**
     * This is used when an object already exists but has been moved within the object graph
     */
    void moveObject(Node parentNode, Object obj);
    void insertObject(Node parentNode, Object obj);

    /**
     * initialize a recently created object with properties entered by the user
     */
    void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);

    /**
     * as above, but this is for cases where there is no node  available
     */
    PropertyChange initObject(ObjectLocation homeLocation, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);

    /**
     * delete/rollback creation of a recently created object
     */
    void deleteObject(ObjectMeta objectMeta);

    void createReference(Node parentNode, Object obj);
    void removeReference(Node referenceNode);

    void moveInList(Node node, int delta);

    void updateReferences(Node node, List<Object> objects);
}
