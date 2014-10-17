package net.sf.xapp.objclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.StandaloneNodeUpdate;
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
    private final ClassDatabase cdb;
    private final ApplicationContainer appContainer;

    public UIUpdater(ClassDatabase cdb, ApplicationContainer appContainer) {
        this.cdb = cdb;
        this.appContainer = appContainer;
    }


    @Override
    public void propertiesChanged(UserId user, Long rev, List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            ObjectMeta objectMeta = cdb.findObjById(changeSet.getObjId());
            Node node = (Node) objectMeta.getAttachment();
            if (node != null) { //todo only refresh if sub-objects have changed
                Node newNode = node.refresh();
                Map<String, PropertyChange> changes = new HashMap<String, PropertyChange>();
                for (PropChange propChange : changeSet.getChanges()) {
                    Property property = objectMeta.getProperty(propChange.getProperty());
                    Object oldVal = property.convert(objectMeta, propChange.getOldValue());
                    Object newVal = property.convert(objectMeta, propChange.getNewValue());
                    changes.put(propChange.getProperty(), new RegularPropertyChange(property, objectMeta.getInstance(), oldVal, newVal));
                }
                appContainer.getApplication().nodeUpdated(node, changes);
                appContainer.expand(newNode);
            }
        }
    }

    @Override
    public void objCreated(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
        appContainer.createNode(StandaloneNodeUpdate.toNode(toObjectLocation(objLoc)), objectMeta);
    }

    @Override
    public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {

    }

    @Override
    public void objMoved(UserId user, Long rev, Long id, ObjLoc newObjLoc) {

    }

    @Override
    public void objDeleted(UserId user, Long rev, Long id) {

    }

    @Override
    public void typeChanged(UserId user, Long rev, ObjLoc objLoc, Long oldId, XmlObj newObj) {

    }

    @Override
    public void objMovedInList(UserId user, Long rev, ObjLoc objLoc, Long id, Integer delta) {

    }

    @Override
    public void refsUpdated(UserId user, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {

    }

    private ObjectLocation toObjectLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb.findObjById(objLoc.getId());
        ObjectLocation objectLocation = new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
        objectLocation.setIndex(objLoc.getIndex());
        return objectLocation;
    }
}
