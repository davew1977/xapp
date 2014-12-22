package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objserver.types.*;
import net.sf.xapp.objserver.types.DeleteConflict;

import static net.sf.xapp.objserver.types.ConflictResolution.*;
import static net.sf.xapp.objserver.types.ConflictStatus.*;

/**
 */
public class ConflictDetector {

    protected final Long localStartId;
    protected Long localIdSeq;
    protected LiveObject liveObject;
    private TrunkState trunkState;
    private BranchState branchState;
    private boolean conflicts;

    protected Map<Long, Revision> deletedObjects = new HashMap<>();
    protected Map<Long, Revision> phantomObjects = new HashMap<>();
    protected Map<Long, Revision> movedObjects = new HashMap<>();
    protected Map<IdProp, Revision> filledObjLocations = new HashMap<>();
    protected Map<IdProp, PotentialPropConflict> propChanges = new HashMap<>();
    protected Map<IdProp, Revision> refChanges = new HashMap<>();

    protected List<PropConflict> propConflicts = new ArrayList<>();
    protected List<MoveConflict> moveConflicts = new ArrayList<>();
    protected Map<Long, AddConflict> addConflicts = new HashMap<>();
    protected Map<Long, DeleteConflict> deleteConflicts = new LinkedHashMap<>();

    protected List<Delta> deltasToApply = new ArrayList<>();



    public ConflictDetector(LiveObject liveObject, Long localIdStart) {
        this.liveObject = liveObject;
        this.localIdSeq = localIdStart;
        this.localStartId = localIdStart;
        trunkState = new TrunkState(this);
        branchState = new BranchState(this);

    }

    public ConflictStatus process(ConflictResolution conflictResolution, List<Revision> trunkDeltas, List<Delta> branchDeltas) {
        for (Revision trunkRevision : trunkDeltas) {
            trunkState.current = trunkRevision;
            trunkRevision.getDelta().getMessage().visit(trunkState);
        }

        for (int i = 0; i < branchDeltas.size(); i++) {
            Delta branchDelta = branchDeltas.get(i);
            branchState.pre(i);
            branchDelta.getMessage().visit(branchState);
            if(branchState.accept(conflictResolution)) {
                deltasToApply.add(branchDelta);
            } else {
                conflicts = true;
            }
        }

        ConflictStatus status = getStatus(conflictResolution);
        //maybe apply some deltas
        if(status != ConflictStatus.NOTHING_COMMITTED) {
            List<Delta> deltasToApply = getDeltasToApply();
            AliasAdaptor aliasAdaptor = new AliasAdaptor(liveObject, localStartId);
            for (Delta delta : deltasToApply) {
                //todo, run this past the conflict detector, because only that will understand how to map local object ids to new object ids

                delta.getMessage().visit(aliasAdaptor);
            }
        }

        return status;
    }

    public List<Delta> getDeltasToApply() {
        return deltasToApply;
    }

    public List<DeleteConflict> getDeleteConflicts() {
        return new ArrayList<>(deleteConflicts.values());
    }

    public List<MoveConflict> getMoveConflicts() {
        return moveConflicts;
    }

    public List<PropConflict> getPropConflicts() {
        return propConflicts;
    }

    public List<AddConflict> getAddConflicts() {
        return new ArrayList<>(addConflicts.values());
    }

    private ConflictStatus getStatus(ConflictResolution resolutionStrategy) {
        if(conflicts) {
            return resolutionStrategy == ABORT_ON_CONFLICT ? NOTHING_COMMITTED :
                    resolutionStrategy == FORCE_ALL_MINE ? CONFLICTS_RESOLVED_MINE :
                            CONFLICTS_RESOLVED_THEIRS;
        } else {
            return NO_CONFLICTS;
        }
    }
}
