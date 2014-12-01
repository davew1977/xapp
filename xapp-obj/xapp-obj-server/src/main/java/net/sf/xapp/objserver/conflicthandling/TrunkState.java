package net.sf.xapp.objserver.conflicthandling;

import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.Revision;

/**
 */
public class TrunkState extends ConflictDetectorState {

    public Revision current;

    public TrunkState(ConflictDetector conflictDetector) {
        super(conflictDetector);
    }

    protected void createObj(ObjLoc objLoc) {
        ObjectLocation objectLocation = toObjLocation(objLoc);
        if(!objectLocation.isCollection()) { //obj creations are ok if the objloc is a collection
            conflictDetector.filledObjLocations.put(toIdProp(objLoc), current);
        }
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            for (PropChange propChange : changeSet.getChanges()) {
                conflictDetector.propChanges.put(new IdProp(objId, propChange.getProperty()), current);
            }
        }
    }

    @Override
    public void deleteObject(UserId principal, ObjLoc objLoc, Long id) {
        conflictDetector.filledObjLocations.remove(toIdProp(objLoc));
        conflictDetector.deletedObjects.put(id, current);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldLoc, ObjLoc newObjLoc) {
        conflictDetector.filledObjLocations.remove(toIdProp(oldLoc));
        conflictDetector.movedObjects.put(id, current);
        createObj(newObjLoc);
    }

    @Override
    public void changeType(UserId principal, ObjLoc objLoc, Long id, Class newType) {
        conflictDetector.filledObjLocations.remove(toIdProp(objLoc));
        conflictDetector.deletedObjects.put(id, current);
    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer newIndex) {
        conflictDetector.movedObjects.put(id, current);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        conflictDetector.refChanges.put(toIdProp(objLoc), current);
    }
}
