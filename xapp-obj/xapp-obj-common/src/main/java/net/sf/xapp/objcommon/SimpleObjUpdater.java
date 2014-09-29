package net.sf.xapp.objcommon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

/**
 * Handles the objupdate api and applies the changes to the class database
 */
public class SimpleObjUpdater implements ObjUpdate {
    protected ClassDatabase cdb;

    public SimpleObjUpdater(ObjectMeta rootObject) {
        this.cdb = rootObject.getClassDatabase();
    }
    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {

        Unmarshaller un = new Unmarshaller(cdb.getClassModel(type));
        un.unmarshalString(xml, Charset.forName("UTF-8"), toObjectLocation(objLoc));
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        cdb.getClassModel(type).newInstance(toObjectLocation(objLoc), true);
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
    }

    @Override
    public void deleteObject(UserId principal, Long id) {
        cdb.findObjById(id).dispose();
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {

        cdb.findObjById(id).setHome(toObjectLocation(newObjLoc), true);
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
        objHome.setIndex(oldIndex);
        ObjectMeta newInstance = cm.newInstance(objHome, true);
        List<Property> properties = cm.getAllProperties();
        for (Property property : properties) {
            newInstance.set(property, obj.get(property));
        }
    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        ObjectMeta obj = cdb.findObjById(id);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        obj.updateIndex(objectLocation, delta);
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
    }

    protected ObjectLocation toObjectLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb.findObjById(objLoc.getId());
        return new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
    }

    protected List<ObjectMeta> lookup(List<Long> ids) {
        List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        for (Long id : ids) {
            result.add(cdb.findObjById(id));
        }
        return result;
    }

    public static XmlObj toXmlObj(ObjectMeta objectMeta) {
        return new XmlObj(objectMeta.getType(), objectMeta.toXml(), objectMeta.getRevision(), objectMeta.getId());
    }
}
