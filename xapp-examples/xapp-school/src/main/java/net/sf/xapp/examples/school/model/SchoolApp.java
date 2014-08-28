package net.sf.xapp.examples.school.model;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyChange;

import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class SchoolApp extends SimpleApplication<SchoolSystem> {

    @Override
    public void init(ApplicationContainer<SchoolSystem> applicationContainer) {
        super.init(applicationContainer);


    }

    @Override
    public void nodeAboutToBeRemoved(Node node, boolean wasCut) {
        System.out.printf("node removed: %s, wasCut=%s, reference=%s\n", node, wasCut, node.isReference());
    }

    @Override
    public void nodeAdded(Node node) {
        System.out.println("node added: " + node);
    }

    @Override
    public void nodeAboutToBeAdded(ObjectLocation homeLocation, ObjectMeta newChild) {
        System.out.printf("node about to be added: location=%s, object=%s\n", homeLocation, newChild.meta());
    }

    @Override
    public void nodeUpdated(Node objectNode, Map<String, PropertyChange> changes) {
        System.out.printf("node updated: %s,  updates: %s\n", objectNode, changes);
    }

    @Override
    public void nodeMovedUp(Node node) {
        System.out.println("node moved up: " + node);
    }

    @Override
    public void nodeMovedDown(Node node) {
        System.out.println("node moved down: " + node);
    }
}
