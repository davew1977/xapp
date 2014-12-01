package net.sf.xapp.objserver;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.util.SimpleCache;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.conflicthandling.ConflictDetector;
import net.sf.xapp.objserver.types.PropConflict;
import net.sf.xapp.objserver.types.ConflictResolution;
import net.sf.xapp.objserver.types.ConflictStatus;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.Revision;
import net.sf.xapp.objserver.types.DeleteConflict;

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
    public void applyChanges(UserId principal, List<Delta> clientDeltas, ConflictResolution strategy, Long baseRevision) {
        List<PropConflict> propConflicts = new ArrayList<>();
        List<DeleteConflict> deleteConflicts = new ArrayList<>();
        if(strategy == ConflictResolution.FORCE_ALL_MINE) {
            //here we just play the deltas on top regardless of conflicts
            for (Delta delta : clientDeltas) {
                InMessage<ObjUpdate, Void> update = delta.getMessage();
                /*try {
                    update.visit(liveObject);
                } catch (ObjMetaNotFoundException e) {
                    treeConflicts.add(new TreeConflict(delta, e.getObjId()));
                }*/
            }
            objManagerReply.applyChangesResponse(principal, propConflicts, ConflictStatus.CONFLICTS_RESOLVED_MINE, deleteConflicts, null);
        } else {
            List<Revision> serverRevs = null;
            ErrorCode errorCode = null;
            try {
                serverRevs = getRevisions(baseRevision, null);
            } catch (GenericException e) {
                objManagerReply.applyChangesResponse(principal, propConflicts, ConflictStatus.NOTHING_COMMITTED, deleteConflicts, e.getErrorCode());
            }
            //figure out which deltas we are in conflict with
            ConflictDetector conflictDetector = new ConflictDetector(liveObject.getRootObj(), serverRevs, clientDeltas);
        }
    }

    @Override
    public void getDeltas(UserId principal, Long revFrom, Long revTo) {
        List<Delta> deltas = null;
        ErrorCode errorCode = null;
        try {
            deltas = getDeltas(revFrom, revTo);
        } catch (GenericException e) {
            errorCode = e.getErrorCode();
        }
        objManagerReply.getDeltasResponse(principal, deltas, liveObject.getType(), revTo, errorCode);
    }

    private List<Delta> getDeltas(Long revFrom, Long revTo) {
        List<Revision> revs = getRevisions(revFrom, revTo);
        List<Delta> deltas = new ArrayList<>();
        for (Revision rev : revs) {
            deltas.add(rev.getDelta());
        }
        return deltas;
    }
    private List<Revision> getRevisions(Long revFrom, Long revTo) {
        Long latestRev = liveObject.getLatestRev();
        if(revFrom > latestRev) {
            throw new GenericException(ErrorCode.DELTA_IS_IN_FUTURE);
        }
        long firstRev = !revisions.isEmpty() ? revisions.keySet().iterator().next() : 0;
        if(revFrom < firstRev) {
            throw new GenericException(ErrorCode.DELTA_TOO_OLD);
        }
        long returnCount = latestRev - revFrom;
        if(returnCount > MAX_DELTAS_RETURNED) {
            throw new GenericException(ErrorCode.TOO_MANY_DELTAS);
        }

        List<Revision> revs = new ArrayList<>();
        for(int i=0; i < returnCount; i++) {
            long r = revFrom + i + 1;
            revs.add(revisions.get(r));
        }
        return revs;
    }

    @Override
    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
        Long rev = liveObject.getLatestRev();
        revisions.put(rev, new Revision(rev, new Delta(inMessage, System.currentTimeMillis()), (UserId) inMessage.principal()));
        return null;
    }
}
