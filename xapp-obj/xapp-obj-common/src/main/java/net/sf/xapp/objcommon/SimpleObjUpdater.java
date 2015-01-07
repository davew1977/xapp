package net.sf.xapp.objcommon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;
import net.sf.xapp.objectmodelling.core.filters.PropertyFilter;
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
    protected ObjMetaWrapper rootObj;

    public SimpleObjUpdater(ObjectMeta rootObject) {
        this.rootObj = new ObjMetaWrapper(rootObject);
    }

    public void setRootObj(ObjectMeta rootObj) {
        this.rootObj = new ObjMetaWrapper(rootObj);
    }

    @Override
    public final void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        Unmarshaller un = new Unmarshaller(cdb().getClassModel(type));
        ObjectMeta objectMeta = un.unmarshalString(xml, Charset.forName("UTF-8"), toObjectLocation(objLoc));
        super.createObject(principal, objLoc, type, xml);
        objectMeta.updateRev(true);
        objAdded(principal, objLoc, objectMeta);
    }

    @Override
    public final void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        ObjectMeta objectMeta = cdb().getClassModel(type).newInstance(toObjectLocation(objLoc), true);
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
            ObjectMeta objectMeta = cdb().findObjById(objId);
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
    public void deleteObject(UserId principal, ObjLoc objLoc, Long id) {
        ObjectMeta objMeta = cdb().findObjById(id);
        ObjectMeta parent = objMeta.getParent();
        objMeta.dispose();
        super.deleteObject(principal, objLoc, id);
        parent.updateRev(); //surely we can't allow deleting root here??? (hence the lack of null check)
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {
        ObjectMeta objMeta = cdb().findObjById(id);
        ObjectMeta oldParent = objMeta.getParent();
        ObjectLocation newHome = toObjectLocation(newObjLoc);
        objMeta.setHome(newHome, true);
        super.moveObject(principal, id, oldObjLoc, newObjLoc);

        oldParent.updateRev();
        newHome.getObj().updateRev();
    }

    @Override
    public void changeType(UserId principal, ObjLoc oldLoc, Long id, Class newType) {
        ObjectMeta obj = cdb().findObjById(id);
        ClassModel cm = cdb().getClassModel(newType);
        if (obj.hasReferences()) {
            /*
            to implement this we need to delete the references(done) and reset them where appropriate (not done)
             */
            throw new UnsupportedOperationException("cannot currently change type on an object which has references");
        }
        ObjectLocation objHome = obj.getHome();
        int oldIndex = obj.homeIndex();
        Map<Property, Object> snapshot = obj.snapshot(PropertyFilter.CONVERTIBLE_TO_STRING);
        obj.dispose();
        objHome.setIndex(oldIndex);
        ObjectMeta newInstance = cm.newInstance(objHome, true);
        List<Property> properties = cm.getAllProperties(PropertyFilter.CONVERTIBLE_TO_STRING);
        for (Property property : properties) {
            newInstance.set(property, snapshot.get(property));
        }
        super.changeType(principal, oldLoc, id, newType);

        newInstance.updateRev();
        typeChanged(principal, new ObjLoc(objHome.getObj().getId(), objHome.getProperty().getName(), oldIndex), id, newInstance);
    }

    protected void typeChanged(UserId principal, ObjLoc objLoc, Long oldId, ObjectMeta newInstance) {

    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer newIndex) {
        ObjectMeta obj = cdb().findObjById(id);
        ObjectLocation objectLocation = toObjectLocation(objLoc);
        obj.setIndex(objectLocation, newIndex);
        super.setIndex(principal, objLoc, id, newIndex);
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
        cdb().setRev(rev);
        updateObject(user, changeSets);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
        cdb().setRev(rev);
        createObject(user, objLoc, obj.getType(), obj.getData());
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void objMoved(UserId user, Long rev, Long id, ObjLoc oldLoc, ObjLoc newObjLoc) {
        cdb().setRev(rev);
        moveObject(user, id, oldLoc, newObjLoc);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void objDeleted(UserId user, Long rev, ObjLoc oldLoc, Long id) {
        cdb().setRev(rev);
        deleteObject(user, oldLoc, id);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void typeChanged(UserId user, Long rev, ObjLoc objLoc, Long id, Class newType, Long newObjId) {
        cdb().setRev(rev);
        changeType(user, objLoc, id, newType);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void objIndexChanged(UserId user, Long rev, ObjLoc objLoc, Long id, Integer newIndex) {
        cdb().setRev(rev);
        setIndex(user, objLoc, id, newIndex);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    @Override
    public void refsUpdated(UserId user, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
        cdb().setRev(rev);
        updateRefs(user, objLoc, refsCreated, refsRemoved);
        assert cdb().getRev().equals(rev) : String.format("%s != %s", cdb().getRev(), rev);
    }

    protected ObjectLocation toObjectLocation(ObjLoc objLoc) {
        return rootObj.toObjLocation(objLoc);
    }

    protected List<ObjectMeta> lookup(List<Long> ids) {
        List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        for (Long id : ids) {
            result.add(cdb().findObjById(id));
        }
        return result;
    }

    public static XmlObj toXmlObj(ObjectMeta objectMeta) {
        return new XmlObj(objectMeta.getType(), objectMeta.toXml(), objectMeta.getRevision(), objectMeta.getId());
    }

    public Long getLatestRev() {
        return cdb().getRev();
    }

    public Class getType() {
        return rootObj.getType();
    }

    public ObjMetaWrapper getRootObj() {
        return rootObj;
    }

    public ClassDatabase cdb(){
        return rootObj.cdb();
    }
}
