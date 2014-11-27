package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.Conflict;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.ObjPropChange;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.Revision;
import net.sf.xapp.objserver.types.TreeConflict;

/**
 */
public class BranchState extends ConflictDetectorState{


    public int current;

    protected BranchState(ConflictDetector conflictDetector) {
        super(conflictDetector);
    }

    @Override
    protected void createObj(ObjLoc objLoc) {
        if(!tryAddTreeConflict(objLoc.getId())) {
            ObjectLocation objectLocation = toObjLocation(objLoc);
            Revision trunkRev = conflictDetector.addedObjects.get(toIdProp(objLoc));
            if(!objectLocation.isCollection() && trunkRev != null) {
                conflictDetector.conflicts.add(new Conflict(current, trunkRev, null));
            }
        }
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        //parse twice, because we must treat the set of updates as atomic
        for (PropChangeSet changeSet : changeSets) {
            if(tryAddTreeConflict(changeSet.getObjId())) {
                return;
            }
        }
        //ok no tree conflicts
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            for (PropChange propChange : changeSet.getChanges()) {
                Revision conflictingRev = conflictDetector.propChanges.get(new IdProp(objId, propChange.getProperty()));
                if(conflictingRev != null) {
                    conflictDetector.conflicts.add(new Conflict(current, conflictingRev, new ObjPropChange(objId, propChange)));
                }
            }
        }

    }

    @Override
    public void deleteObject(UserId principal, ObjLoc objLoc, Long id) {
        tryAddTreeConflict(id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {

    }

    @Override
    public void changeType(UserId principal, ObjLoc objLoc, Long id, Class newType) {

    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer newIndex) {

    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {

    }

    private boolean tryAddTreeConflict(Long id) {
        Revision revision = conflictDetector.deletedObjects.get(id);
        if(revision != null) {
            TreeConflict treeConflict = conflictDetector.treeConflicts.get(id);
            if (treeConflict == null) {
                treeConflict = new TreeConflict(new ArrayList<Integer>(), revision, id);
                conflictDetector.treeConflicts.put(id, treeConflict);
            }
            treeConflict.getMyDeltaIndexes().add(current);
        }
        return revision != null;
    }
}
