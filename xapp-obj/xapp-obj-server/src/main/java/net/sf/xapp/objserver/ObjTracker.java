package net.sf.xapp.objserver;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.util.SimpleCache;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.Conflict;
import net.sf.xapp.objserver.types.ConflictResolution;
import net.sf.xapp.objserver.types.ConflictStatus;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.Revision;
import net.sf.xapp.objserver.types.TreeConflict;
import net.sf.xapp.utils.ObjMetaNotFoundException;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class ObjTracker extends ObjListenerAdaptor implements ObjManager {
    public static final int MAX_SIZE = 1000;//
    public static final int MAX_DELTAS_RETURNED = 100;//
    private final LiveObject liveObject;
    private final ObjManagerReply objManagerReply;
    private SimpleCache<Long, Revision> revisions;

    public ObjTracker(LiveObject liveObject, ObjManagerReply objManagerReply) {
        this.liveObject = liveObject;
        this.objManagerReply = objManagerReply;
        revisions = new SimpleCache<Long, Revision>(MAX_SIZE);
    }

    @Override
    public void getObject(UserId principal) {
        objManagerReply.getObjectResponse(principal, liveObject.createXmlObj(), null);
    }

    @Override
    public void applyChanges(UserId principal, List<Delta> deltas, ConflictResolution conflictResolutionStrategy, Long baseRevision) {
        if(conflictResolutionStrategy == ConflictResolution.FORCE_ALL_MINE) {
            List<TreeConflict> treeConflicts = new ArrayList<>();
            //here we just play the deltas on top regardless of conflicts
            for (Delta delta : deltas) {
                InMessage<ObjUpdate, Void> update = delta.getMessage();
                try {
                    update.visit(liveObject);
                } catch (ObjMetaNotFoundException e) {
                    treeConflicts.add(new TreeConflict(delta, e.getObjId()));
                }
            }
            List<Conflict> conflicts = new ArrayList<>();
            objManagerReply.applyChangesResponse(principal, conflicts, ConflictStatus.CONFLICTS_RESOLVED_MINE, treeConflicts, null);
        }
    }

    @Override
    public void getDeltas(UserId principal, Long revFrom, Long revTo) {
        Long latestRev = liveObject.getLatestRev();
        if(revFrom > latestRev) {
            objManagerReply.getDeltasResponse(principal, null, liveObject.getType(), revTo, ErrorCode.DELTA_IS_IN_FUTURE);
            return;
        }
        long firstRev = !revisions.isEmpty() ? revisions.keySet().iterator().next() : 0;
        if(revFrom < firstRev) {
            objManagerReply.getDeltasResponse(principal, null, liveObject.getType(), revTo, ErrorCode.DELTA_TOO_OLD);
            return;
        }

        long returnCount = latestRev - revFrom;
        if(returnCount > MAX_DELTAS_RETURNED) {
            objManagerReply.getDeltasResponse(principal, null, liveObject.getType(), revTo, ErrorCode.TOO_MANY_DELTAS);
            return;
        }

        List<Delta> deltas = new ArrayList<Delta>();
        for(int i=0; i < returnCount; i++) {
            long r = revFrom + i + 1;
            deltas.add(revisions.get(r).getDelta());
        }
        objManagerReply.getDeltasResponse(principal, deltas, liveObject.getType(), revTo, null);
    }

    @Override
    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
        Long rev = liveObject.getLatestRev();
        revisions.put(rev, new Revision(rev, new Delta(inMessage)));
        return null;
    }
}
