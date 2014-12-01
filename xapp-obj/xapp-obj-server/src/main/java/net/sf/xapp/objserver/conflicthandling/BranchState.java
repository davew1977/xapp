package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.types.*;
import net.sf.xapp.objserver.types.PropConflict;

/**
 */
public class BranchState extends ConflictDetectorState{


    public int current;

    protected BranchState(ConflictDetector conflictDetector) {
        super(conflictDetector);
    }

    @Override
    protected void createObj(ObjLoc objLoc) {
        if(!tryAddDeleteConflict(objLoc.getId())) {
            ObjectLocation objectLocation = toObjLocation(objLoc);
            Revision trunkRev = conflictDetector.filledObjLocations.get(toIdProp(objLoc));
            if(!objectLocation.isCollection() && trunkRev != null) {
                conflictDetector.propConflicts.add(new PropConflict(current, trunkRev, null));
            }
        }
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        //parse twice, because we must treat the set of updates as atomic
        for (PropChangeSet changeSet : changeSets) {
            if(tryAddDeleteConflict(changeSet.getObjId())) {
                return;
            }
        }
        //ok no tree conflicts
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            for (PropChange propChange : changeSet.getChanges()) {
                Revision conflictingRev = conflictDetector.propChanges.get(new IdProp(objId, propChange.getProperty()));
                if(conflictingRev != null) {
                    conflictDetector.propConflicts.add(new PropConflict(current, conflictingRev, new ObjPropChange(objId, propChange)));
                }
            }
        }

    }

    @Override
    public void deleteObject(UserId principal, ObjLoc objLoc, Long id) {
        tryAddDeleteConflict(id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {
        if(!tryAddDeleteConflict(id) && !tryAddDeleteConflict(newObjLoc.getId())) {
            Revision conflictingRev = conflictDetector.movedObjects.get(id);
            if(conflictingRev != null) {
                conflictDetector.co
            }
        }
    }

    @Override
    public void changeType(UserId principal, ObjLoc objLoc, Long id, Class newType) {
        tryAddDeleteConflict(id);
    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer newIndex) {
        if(!tryAddDeleteConflict(id) && !tryAddDeleteConflict(objLoc.getId())) {
           conflictDetector.movedObjects
        }
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        if(!tryAddDeleteConflict(objLoc.getId())) {

        }
    }

    private boolean tryAddDeleteConflict(Long id) {
        Revision revision = conflictDetector.deletedObjects.get(id);
        if(revision != null) {
            DeleteConflict deleteConflict = conflictDetector.deleteConflicts.get(id);
            if (deleteConflict == null) {
                deleteConflict = new DeleteConflict(new ArrayList<Integer>(), revision, id);
                conflictDetector.deleteConflicts.put(id, deleteConflict);
            }
            deleteConflict.getMyDeltaIndexes().add(current);
        }
        return revision != null;
    }
}
