package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.PropertyUpdate;

import java.util.List;

/**
 * acts as a bridge between the UI and the application controller. In classic XAPP, these notification simply feed
 * through to the client side application. In Xapp Cloud the notifications will be sent to the server, which will decide
 * when and how to update the clients
 */
public interface NodeUpdateApi {

    void updateNode(Node node, List<PropertyUpdate> potentialUpdates);
    /**
     * index has a value if the containing property is a list (null if map or set)
     */
    void addNode(Node node);
    void removeNode(Node node);
    /**
     * Node was moved up or down in a list
     */
    void changeNodeIndex(Node node, int oldIndex, int newIndex);
}
