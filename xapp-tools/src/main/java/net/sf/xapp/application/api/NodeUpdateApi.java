package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.*;

import java.nio.charset.Charset;
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
    void createObject(ObjectLocation homeLocation, ClassModel type, ObjCreateCallback callback);

    /**
     * This is used when an object already exists but has been moved within the object graph, or introduced to it for
     * the first time
     */
    void moveOrInsertObjMeta(ObjectLocation objectLocation, ObjectMeta objMeta);
    void insertObject(ObjectLocation objectLocation, Object obj);

    /**
     * initialize a recently created object with properties entered by the user
     */
    void initObject(Node parentNode, ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);

    /**
     * as above, but this is for cases where there is no node  available
     */
    PropertyChange initObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);

    /**
     * delete/rollback creation of a recently created object
     */
    void deleteObject(ObjectMeta objectMeta);

    void moveInList(ObjectLocation objectLocation, ObjectMeta objectMeta, int delta);

    void updateReferences(ObjectLocation objectLocation, List<ObjectMeta> refsToAdd, List<ObjectMeta> refsToRemove);

    void changeType(ObjectMeta obj, ClassModel targetClassModel);

    ObjectMeta deserializeAndInsert(ObjectLocation objectLocation, ClassModel classModel, String xml, Charset charset);
}
