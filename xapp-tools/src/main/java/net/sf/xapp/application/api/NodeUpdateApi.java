package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

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

    void addObject(ObjectMeta objectMeta, List<PropertyUpdate> potentialUpdates);
    void addObject(ObjectMeta objectMeta);
    void removeObject(ObjectMeta objectMeta);
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

    ObjectMeta createObject(Node parentNode, ClassModel type);
    ObjectMeta registerObject(Node parentNode, ClassModel type, Object obj);

    void cancelObject(ObjectMeta objMeta);

}
