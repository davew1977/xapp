package net.sf.xapp.objserver.conflicthandling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xapp.objcommon.ObjMetaWrapper;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;

/**
 */
public class ConflictDetector {

    protected ObjMetaWrapper rootObj;
    private TrunkState trunkState;
    private BranchState branchState;
    protected Set<Long> deletedObjects = new HashSet<>();
    protected Set<Long> phantomObjects = new HashSet<>(); //these objects could not be created by the new delta stream - negative numbers
    protected Map<IdProp, Delta> deltaMap = new HashMap<>();

    protected Delta current;

    public ConflictDetector(ObjMetaWrapper rootObj, List<Delta> trunkDeltas, List<Delta> branchDeltas) {
        this.rootObj = rootObj;
        trunkState = new TrunkState(this);
        branchState = new BranchState(this);

        process(trunkDeltas, branchDeltas);
    }

    public void process(List<Delta> trunkDeltas, List<Delta> branchDeltas) {
        for (Delta trunkDelta : trunkDeltas) {
            current = trunkDelta;
            trunkDelta.getMessage().visit(trunkState);
        }
        for (Delta branchDelta : branchDeltas) {
            current = branchDelta;
            branchDelta.getMessage().visit(branchState);
        }
    }
}
