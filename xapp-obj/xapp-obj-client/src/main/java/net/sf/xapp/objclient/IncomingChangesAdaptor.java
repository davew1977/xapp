package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.api.StandaloneNodeUpdate;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class IncomingChangesAdaptor implements ObjListener {
    private NodeUpdateApi nodeUpdateApi;
    private ClassDatabase cdb;

    public IncomingChangesAdaptor(ApplicationContainer appContainer) {
        this.nodeUpdateApi = new StandaloneNodeUpdate(appContainer);
        this.cdb = appContainer.getClassDatabase();
    }

    @Override
    public void propertiesChanged(List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            ObjectMeta objectMeta = cdb.findObjById(objId);
            List<PropertyUpdate> updates = new ArrayList<PropertyUpdate>();
            for (PropChange propChange : changeSet.getChanges()) {
                Property property = objectMeta.getProperty(propChange.getProperty());
                Object oldVal = property.convert(objectMeta, propChange.getOldValue());
                Object newVal = property.convert(objectMeta, propChange.getNewValue());
                updates.add(new PropertyUpdate(property, oldVal, newVal));
            }
            nodeUpdateApi.updateObject(objectMeta, updates);
        }
    }

    @Override
    public void objAdded(ObjLoc objLoc, XmlObj obj) {
        nodeUpdateApi.deserializeAndInsert(toObjectLocation(objLoc),
                cdb.getClassModel(obj.getType()), obj.getData(), Charset.forName("UTF-8"));
    }

    @Override
    public void objMoved(Long id, ObjLoc newObjLoc) {
        ObjectMeta objectMeta = cdb.findObjById(id);
        nodeUpdateApi.moveOrInsertObjMeta(toObjectLocation(newObjLoc), objectMeta);
    }

    @Override
    public void objDeleted(Long id) {
        nodeUpdateApi.deleteObject(cdb.findObjById(id));
    }

    @Override
    public void refsUpdated(ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
       nodeUpdateApi.updateReferences(toObjectLocation(objLoc), lookup(refsCreated), lookup(refsRemoved));
    }

    @Override
    public void typeChanged(Long id, Class newType) {
       nodeUpdateApi.changeType(cdb.findObjById(id), cdb.getClassModel(newType));
    }

    @Override
    public void objMovedInList(ObjLoc objLoc, Long id, Integer delta) {
        nodeUpdateApi.moveInList(toObjectLocation(objLoc), cdb.findObjById(id), delta);
    }

    private ObjectLocation toObjectLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb.findObjById(objLoc.getId());
        return new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
    }

    private List<ObjectMeta> lookup(List<Long> ids) {
        List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        for (Long id : ids) {
            result.add(cdb.findObjById(id));
        }
        return result;
    }
}
