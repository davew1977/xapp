package net.sf.xapp.objserver.conflicthandling;

import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.*;

/**
 */
public class TrunkState extends ConflictDetectorState implements ObjListener{

    public Revision current;

    public TrunkState(ConflictDetector conflictDetector) {
        super(conflictDetector);
    }

    @Override
    public void objAdded(UserId user, Long rev, ObjLoc objLoc, XmlObj obj) {
        ObjectLocation objectLocation = toObjLocation(objLoc);
        if(!objectLocation.isCollection()) { //obj creations are ok if the objloc is a collection
            conflictDetector.filledObjLocations.put(toIdProp(objLoc), current);
        }
    }

    @Override
    public void propertiesChanged(UserId user, Long rev, List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            for (PropChange propChange : changeSet.getChanges()) {
                ObjectMeta objectMeta = conflictDetector.liveObject.cdb().findObjById(objId);
                if (objectMeta != null) {
                    conflictDetector.propChanges.put(new IdProp(objId, propChange.getProperty()),
                            new PotentialPropConflict(current, propChange, createObjInfo(objectMeta)));
                } //else we will get a delete conflict instead
            }
        }
    }

    @Override
    public void objDeleted(UserId user, Long rev, ObjLoc oldObjLoc, Long id) {
        conflictDetector.filledObjLocations.remove(toIdProp(oldObjLoc));
        conflictDetector.deletedObjects.put(id, current);
    }

    @Override
    public void typeChanged(UserId user, Long rev, ObjLoc objLoc, Long deletedObjId, Class newType, Long newObjId) {
        //todo
    }

    @Override
    public void objMoved(UserId user, Long rev, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {
        conflictDetector.filledObjLocations.remove(toIdProp(oldObjLoc));
        conflictDetector.movedObjects.put(id, current);
        objAdded(null, null, newObjLoc, null);
    }

    @Override
    public void objIndexChanged(UserId user, Long rev, ObjLoc objLoc, Long id, Integer delta) {
        conflictDetector.movedObjects.put(id, current);
    }

    @Override
    public void refsUpdated(UserId user, Long rev, ObjLoc objLoc, List<Long> refsCreated, List<Long> refsRemoved) {
        conflictDetector.refChanges.put(toIdProp(objLoc), current);
    }
}
