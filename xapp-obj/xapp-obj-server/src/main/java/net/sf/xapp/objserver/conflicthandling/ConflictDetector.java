package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xapp.objcommon.ObjMetaWrapper;
import net.sf.xapp.objserver.types.Conflict;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.Revision;
import net.sf.xapp.objserver.types.TreeConflict;

/**
 */
public class ConflictDetector {

    protected ObjMetaWrapper rootObj;
    private TrunkState trunkState;
    private BranchState branchState;

    protected Set<Long> phantomObjects = new HashSet<>(); //these objects could not be created by the new delta stream - negative numbers

    protected Map<Long, Revision> deletedObjects = new HashMap<>();
    protected Map<Long, Revision> movedObjects = new HashMap<>();
    protected Map<IdProp, Revision> addedObjects = new HashMap<>();
    protected Map<IdProp, Revision> propChanges = new HashMap<>();
    protected Map<IdProp, Revision> indexChanges = new HashMap<>();
    protected Map<IdProp, Revision> refChanges = new HashMap<>();

    protected List<Conflict> conflicts = new ArrayList<>();
    protected Map<Long, TreeConflict> treeConflicts = new LinkedHashMap<>();


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
        for (Delta branchDelta : branchDeltas) {
            branchState.current = branchDelta;
            branchDelta.getMessage().visit(branchState);
        }
    }
}
