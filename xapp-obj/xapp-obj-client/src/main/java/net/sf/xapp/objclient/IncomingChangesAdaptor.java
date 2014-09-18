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
    public void propertiesChanged(UserId userId, List<PropChangeSet> changeSets) {
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
    public void objAdded(UserId userId, ObjLoc objLoc, XmlObj obj) {
        insertObj(objLoc, obj);
    }

    @Override
    public void objCreated(UserId userId, ObjLoc objLoc, XmlObj obj) {
        ObjectMeta objectMeta = insertObj(objLoc, obj);
        if(userId.equals(thisUserId)){
            objCreateCallback.objCreated(objectMeta);
            objCreateCallback=null;
        }
    }

    @Override
    public void objMoved(UserId userId, Long id, ObjLoc newObjLoc) {
        ObjectMeta objectMeta = cdb.findObjById(id);
        nodeUpdateApi.moveOrInsertObjMeta(toObjectLocation(newObjLoc), objectMeta);
    }

    @Override
    public void objDeleted(UserId userId, Long id) {
        nodeUpdateApi.deleteObject(cdb.findObjById(id));
    }

    @Override
    public void refsUpdated(UserId userId, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
       nodeUpdateApi.updateReferences(toObjectLocation(objLoc), lookup(refsCreated), lookup(refsRemoved));
    }

    @Override
    public void typeChanged(UserId userId, Long id, Class newType) {
       nodeUpdateApi.changeType(cdb.findObjById(id), cdb.getClassModel(newType));
    }

    @Override
    public void objMovedInList(UserId userId, ObjLoc objLoc, Long id, Integer delta) {
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

    private ObjectMeta insertObj(ObjLoc objLoc, XmlObj obj) {
        return nodeUpdateApi.deserializeAndInsert(toObjectLocation(objLoc),
                cdb.getClassModel(obj.getType()), obj.getData(), Charset.forName("UTF-8"));
    }
}
