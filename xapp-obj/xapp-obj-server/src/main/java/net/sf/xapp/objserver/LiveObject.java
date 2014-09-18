package net.sf.xapp.objserver;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LiveObject implements ObjUpdate {
    private ObjListener listener;
    private ClassDatabase cdb;

    public LiveObject(ObjectMeta rootObject) {
        this.cdb = rootObject.getClassDatabase();
    }

    public void setListener(ObjListener listener) {
        this.listener = listener;
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {

        Unmarshaller un = new Unmarshaller(cdb.getClassModel(type));
        un.setMaster(true);
        ObjectMeta objectMeta = un.unmarshalString(xml, Charset.forName("UTF-8"), toObjectLocation(objLoc));
        listener.objAdded(principal, objLoc, new XmlObj(type, objectMeta.toXml(), 0L, objectMeta.getId()));
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        ObjectMeta objectMeta = cdb.getClassModel(type).newInstance(toObjectLocation(objLoc), true, cdb.nextId());
        listener.objCreated(principal, objLoc, new XmlObj(type, objectMeta.toXml(), 0L, objectMeta.getId()));
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
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
            objectMeta.update(updates);
        }
        listener.propertiesChanged(principal, changeSets);
    }

    @Override
    public void deleteObject(UserId principal, Long id) {
        cdb.findObjById(id).dispose();
        listener.objDeleted(principal, id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {

        cdb.findObjById(id).setHome(toObjectLocation(newObjLoc), true);

        listener.objMoved(principal, id, newObjLoc);
    }

    @Override
    public void changeType(UserId principal, Long id, Class newType) {
        ObjectMeta obj = cdb.findObjById(id);
        ClassModel cm = cdb.getClassModel(newType);
        if (obj.hasReferences()) {
            /*
            to implement this we need to delete the references(done) and reset them where appropriate (not done)
             */
            throw new UnsupportedOperationException("cannot currently change type on an object which has references");
        }
        ObjectLocation objHome = obj.getHome();
        int oldIndex = obj.index();
        obj.dispose();
        ObjectMeta newInstance = cm.newInstance(objHome, true, id);
        List<Property> properties = cm.getAllProperties();
        for (Property property : properties) {
            newInstance.set(property, obj.get(property));
        }

        objHome.setIndex(newInstance, oldIndex);
        listener.typeChanged(principal, id, newType);
    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        ObjectMeta obj = cdb.findObjById(id);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        obj.updateIndex(objectLocation, delta);
        listener.objMovedInList(principal, objLoc, id, delta);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        List<ObjectMeta> toRemove = lookup(refsToRemove);
        List<ObjectMeta> toAdd = lookup(refsToAdd);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        for (ObjectMeta objectMeta : toRemove) {
            objectMeta.removeAndUnsetReference(objectLocation);
        }
        for (ObjectMeta objectMeta : toAdd) {
            objectMeta.createAndSetReference(objectLocation);
        }

        listener.refsUpdated(principal, objLoc, refsToAdd, refsToRemove);
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
