package net.sf.xapp.objcommon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

/**
 * Handles the objupdate api and applies the changes to the class database
 */
public class SimpleObjUpdater extends ObjUpdateAdaptor implements ObjUpdate, ObjListener {
    protected ObjectMeta rootObj;
    protected ClassDatabase cdb;
    protected boolean incrementRevisions;

    public SimpleObjUpdater(ObjectMeta rootObject, boolean incrementRevisions) {
        this.incrementRevisions = incrementRevisions;
        this.cdb = rootObject.getClassDatabase();
        this.rootObj = rootObject;
    }
    @Override
    public final void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        Unmarshaller un = new Unmarshaller(cdb.getClassModel(type));
        ObjectMeta objectMeta = un.unmarshalString(xml, Charset.forName("UTF-8"), toObjectLocation(objLoc));
        super.createObject(principal, objLoc, type, xml);
        objectMeta.updateRev(true);
        objAdded(principal, objLoc, objectMeta);
    }

    @Override
    public final void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        ObjectMeta objectMeta = cdb.getClassModel(type).newInstance(toObjectLocation(objLoc), true);
        super.createEmptyObject(principal, objLoc, type);
        objectMeta.updateRev();
        objAdded(principal, objLoc, objectMeta);
    }

    /**
     * override this instead of the 2 create methods from the ObjUpdate interface
     */
    protected void objAdded(UserId principal, ObjLoc objLoc, ObjectMeta objectMeta) {

    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        List<ObjectMeta> changed = new ArrayList<ObjectMeta>();
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
            changed.add(objectMeta);
        }
        super.updateObject(principal, changeSets);
        for (ObjectMeta objectMeta : changed) {
            objectMeta.updateRev();
        }
    }

    @Override
    public void deleteObject(UserId principal, Long id) {
        ObjectMeta objMeta = cdb.findObjById(id);
        ObjectMeta parent = objMeta.getParent();
        objMeta.dispose();
        super.deleteObject(principal, id);
        parent.updateRev(); //surely we can't allow deleting root here??? (hence the lack of null check)
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {
        ObjectMeta objMeta = cdb.findObjById(id);
        ObjectMeta oldParent = objMeta.getParent();
        ObjectLocation newHome = toObjectLocation(newObjLoc);
        objMeta.setHome(newHome, true);
        super.moveObject(principal, id, newObjLoc);

        oldParent.updateRev();
        newHome.getObj().updateRev();
    }

    @Override
    public final void changeType(UserId principal, Long id, Class newType) {
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
        super.changeType(principal, id, newType);

        newInstance.updateRev();
        typeChanged(principal, id, newInstance);
    }

    protected void typeChanged(UserId principal, Long oldId, ObjectMeta newInstance) {

    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        ObjectMeta obj = cdb.findObjById(id);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        obj.updateIndex(objectLocation, delta);
        super.moveInList(principal, objLoc, id, delta);
        objectLocation.getObj().updateRev();
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
        super.updateRefs(principal, objLoc, refsToAdd, refsToRemove);
        objectLocation.getObj().updateRev();
    }

    @Override
    public void propertiesChanged(UserId user, Long rev, List<PropChangeSet> changeSets) {
        updateObject(user, changeSets);
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
        createObject(user, objLoc, obj.getType(), obj.getData());
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void objMoved(UserId user, Long rev, Long id, ObjLoc newObjLoc) {
        moveObject(user, id, newObjLoc);
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void objDeleted(UserId user, Long rev, Long id) {
        deleteObject(user, id);
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void typeChanged(UserId user, Long rev, ObjLoc objLoc, Long oldId, XmlObj newObj) {
        changeType(user, oldId, newObj.getType());
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void objMovedInList(UserId user, Long rev, ObjLoc objLoc, Long id, Integer delta) {
        moveInList(user, objLoc, id, delta);
        assert cdb.getRev().equals(rev);
    }

    @Override
    public void refsUpdated(UserId user, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
        updateRefs(user, objLoc, refsCreated, refsRemoved);
        assert cdb.getRev().equals(rev);
    }

    @Override
    public <T> T handleMessage(InMessage<ObjUpdate, T> inMessage) {
        if (incrementRevisions) {
            cdb.incrementRevision();
        }
        return null;
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

    public Long getLatestRev() {
        return cdb.getRev();
    }

    public Class getType() {
        return rootObj.getType();
    }

    public ObjectMeta getRootObj() {
        return rootObj;
    }
}
