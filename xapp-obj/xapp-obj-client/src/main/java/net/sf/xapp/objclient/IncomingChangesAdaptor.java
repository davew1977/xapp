package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.api.ObjCreateCallback;
import net.sf.xapp.application.api.StandaloneNodeUpdate;
import net.sf.xapp.net.common.types.UserId;
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
    private UserId thisUserId;
    private ObjCreateCallback objCreateCallback;

    public IncomingChangesAdaptor(ApplicationContainer appContainer, ObjClientContext clientContext) {
        this.nodeUpdateApi = new StandaloneNodeUpdate(appContainer);
        this.cdb = appContainer.getClassDatabase();
        this.thisUserId = clientContext.getUserId();
    }

    public void setObjCreateCallback(ObjCreateCallback objCreateCallback) {
        if(this.objCreateCallback != null) {
            throw new IllegalArgumentException("One create at a time!");
        }
        this.objCreateCallback = objCreateCallback;
    }

    @Override
    public void propertiesChanged(UserId userId, Long rev, List<PropChangeSet> changeSets) {
        cdb.setRevision(rev);
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
            objectMeta.updateRev();
        }
    }

    @Override
    public void objAdded(UserId userId, Long rev, ObjLoc objLoc, XmlObj obj) {
        cdb.setRevision(rev);
        ObjectMeta objectMeta = insertObj(objLoc, obj);
        objectMeta.updateRev();
    }

    @Override
    public void objCreated(UserId userId, Long rev, ObjLoc objLoc, XmlObj obj) {
        cdb.setRevision(rev);
        ObjectMeta objectMeta = insertObj(objLoc, obj);
        objectMeta.updateRev();

        if(userId.equals(thisUserId)){
            objCreateCallback.objCreated(objectMeta);
            objCreateCallback=null;
        }
    }

    @Override
    public void objMoved(UserId userId, Long rev, Long id, ObjLoc newObjLoc) {
        cdb.setRevision(rev);
        ObjectMeta objectMeta = cdb.findObjById(id);
        ObjectLocation objectLocation = toObjectLocation(newObjLoc);
        nodeUpdateApi.moveOrInsertObjMeta(objectLocation, objectMeta);

        objectLocation.getObj().updateRev();
    }

    @Override
    public void objDeleted(UserId userId, Long rev, Long id) {
        cdb.setRevision(rev);
        ObjectMeta objMeta = cdb.findObjById(id);
        nodeUpdateApi.deleteObject(objMeta);

        objMeta.updateRev();
    }

    @Override
    public void refsUpdated(UserId userId, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
        cdb.setRevision(rev);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        nodeUpdateApi.updateReferences(objectLocation, lookup(refsCreated), lookup(refsRemoved));

        objectLocation.getObj().updateRev();
    }

    @Override
    public void typeChanged(UserId user, Long rev, ObjLoc objLoc, Long oldId, XmlObj newObj) {
        cdb.setRevision(rev);
        //remove old object
        nodeUpdateApi.deleteObject(cdb.findObjById(oldId));
        ObjectMeta objectMeta = insertObj(objLoc, newObj);
        objectMeta.updateRev();
    }

    @Override
    public void objMovedInList(UserId userId, Long rev, ObjLoc objLoc, Long id, Integer delta) {
        cdb.setRevision(rev);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        nodeUpdateApi.moveInList(objectLocation, cdb.findObjById(id), delta);
        objectLocation.getObj().updateRev();
    }

    private ObjectLocation toObjectLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb.findObjById(objLoc.getId());
        ObjectLocation objectLocation = new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
        objectLocation.setIndex(objLoc.getIndex());
        return objectLocation;
    }

    private List<ObjectMeta> lookup(List<Long> ids) {
        List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        for (Long id : ids) {
            result.add(cdb.findObjById(id));
        }
        return result;
    }

    private ObjectMeta insertObj(ObjLoc objLoc, XmlObj obj) {
        return nodeUpdateApi.deserializeAndInsert(toObjectLocation(objLoc),
                cdb.getClassModel(obj.getType()), obj.getData(), Charset.forName("UTF-8"));
    }
}
