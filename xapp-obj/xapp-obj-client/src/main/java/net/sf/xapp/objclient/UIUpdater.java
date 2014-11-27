package net.sf.xapp.objclient;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.ObjCreateCallback;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.RegularPropertyChange;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

/**
 * We assume model is already updated and our responsibility is to update the App UI
 */
public class UIUpdater implements ObjListener {
    private final GUIObjClient objClient;
    private ObjCreateCallback objCreateCallback;

    public UIUpdater(GUIObjClient objClient) {
        this.objClient = objClient;
    }

    public void setObjCreateCallback(ObjCreateCallback objCreateCallback) {
        if(this.objCreateCallback != null) {
            throw new IllegalArgumentException("One create at a time!");
        }
        this.objCreateCallback = objCreateCallback;
    }


    @Override
    public void propertiesChanged(UserId user, Long rev, List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            ObjectMeta objectMeta = cdb().findObjById(changeSet.getObjId());
            Node node = appContainer().getNodeBuilder().getNode(changeSet.getObjId());
            if (node != null) { //todo only refresh if sub-objects have changed
                Node newNode = node.refresh();
                Map<String, PropertyChange> changes = new HashMap<String, PropertyChange>();
                for (PropChange propChange : changeSet.getChanges()) {
                    Property property = objectMeta.getProperty(propChange.getProperty());
                    Object oldVal = property.convert(objectMeta, propChange.getOldValue());
                    Object newVal = property.convert(objectMeta, propChange.getNewValue());
                    changes.put(propChange.getProperty(), new RegularPropertyChange(property, objectMeta.getInstance(), oldVal, newVal));
                }
                appContainer().getApplication().nodeUpdated(node, changes);
                appContainer().expand(newNode);
            }
        }
    }

    @Override
    public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
        appContainer().createNode(toObjectLocation(objLoc), obj(obj));
    }

    @Override
    public void objMoved(UserId user, Long rev, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {
        ObjectMeta objectMeta = cdb().findObjById(id);
        appContainer().removeNode(id);
        //create new node
        appContainer().createNode(toObjectLocation(newObjLoc), objectMeta);
    }

    @Override
    public void objDeleted(UserId user, Long rev, ObjLoc objLoc,  Long id) {
        //clean up nodes
        Collection<Node> refNodes = appContainer().getRefNodes(id);

        appContainer().removeNode(id);

        for (Node referencingNode : refNodes) {
            appContainer().removeNode(referencingNode);
        }
    }

    @Override
    public void objIndexChanged(UserId user, Long rev, ObjLoc objLoc, Long id, Integer newIndex) {
        ObjectMeta objectMeta = obj(id);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        Node node = appContainer().getNode(id, objectLocation);
        node.updateIndex(objectLocation.indexOf(objectMeta));
        if(objClient.getUserId().equals(user)) {
            appContainer().setSelectedNode(node);
        }
    }

    private ApplicationContainer appContainer() {
        return objClient.getAppContainer();
    }

    @Override
    public void refsUpdated(UserId user, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        for (Long id : refsRemoved) {
            Node refNode = appContainer().getNode(id, objectLocation);
            appContainer().removeNode(refNode);
        }
        for (Long id : refsCreated) {
            appContainer().createNode(objectLocation, obj(id));
        }
    }

    private ObjectLocation toObjectLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb().findObjById(objLoc.getId());
        ObjectLocation objectLocation = new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
        objectLocation.setIndex(objLoc.getIndex());
        return objectLocation;
    }

    private ObjectMeta obj(long id) {
        return cdb().findObjById(id);
    }

    private ClassDatabase cdb() {
        return objClient.getCdb();
    }

    private ObjectMeta obj(XmlObj newObj) {
        return obj(newObj.getId());
    }
}
