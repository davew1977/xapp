package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xapp.objcommon.ObjMetaWrapper;
import net.sf.xapp.objserver.types.*;
import net.sf.xapp.objserver.types.DeleteConflict;

/**
 */
public class ConflictDetector {

    protected ObjMetaWrapper rootObj;
    private TrunkState trunkState;
    private BranchState branchState;

    protected Set<Long> phantomObjects = new HashSet<>(); //these objects could not be created by the new delta stream - negative numbers

    protected Map<Long, Revision> deletedObjects = new HashMap<>();
    protected Map<Long, Revision> movedObjects = new HashMap<>();
    protected Map<IdProp, Revision> filledObjLocations = new HashMap<>();
    protected Map<IdProp, Revision> propChanges = new HashMap<>();
    protected Map<IdProp, Revision> refChanges = new HashMap<>();

    protected List<PropConflict> propConflicts = new ArrayList<>();
    protected List<MoveConflict> moveConflicts = new ArrayList<>();
    protected Map<Long, DeleteConflict> deleteConflicts = new LinkedHashMap<>();


    public ConflictDetector(ObjMetaWrapper rootObj, List<Revision> trunkRevisions, List<Delta> branchDeltas) {
        this.rootObj = rootObj;
        trunkState = new TrunkState(this);
        branchState = new BranchState(this);

        process(trunkRevisions, branchDeltas);
    }

    public void process(List<Revision> trunkDeltas, List<Delta> branchDeltas) {
        for (Revision trunkRevision : trunkDeltas) {
            trunkState.current = trunkRevision;
            trunkRevision.getDelta().getMessage().visit(trunkState);
        }
        for (int i = 0; i < branchDeltas.size(); i++) {
            Delta branchDelta = branchDeltas.get(i);
            branchState.current = i;
            branchDelta.getMessage().visit(branchState);
        }
    }
}
