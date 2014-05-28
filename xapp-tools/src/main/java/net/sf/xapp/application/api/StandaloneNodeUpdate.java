package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class StandaloneNodeUpdate implements NodeUpdateApi {
    @Override
    public void updateNode(Node node, List<PropertyUpdate> potentialUpdates) {
        ApplicationContainer appContainer = node.getApplicationContainer();
        Node newNode = appContainer.getNodeBuilder().refresh(node);
        appContainer.getApplication().nodeUpdated(newNode, PropertyUpdate.execute(potentialUpdates));
    }

    @Override
    public void addNode(Node node) {

    }

    @Override
    public void removeNode(Node node) {

    }

    @Override
    public void changeNodeIndex(Node node, int oldIndex, int newIndex) {

    }
}
