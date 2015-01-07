package net.sf.xapp.objserver;

import java.util.List;

import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.channels.AppAdaptor;
import net.sf.xapp.net.server.channels.BroadcastProxy;
import net.sf.xapp.net.server.channels.CommChannel;
import net.sf.xapp.net.server.channels.NotifyProxy;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objcommon.MasterObject;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReplyAdaptor;
import net.sf.xapp.objserver.types.ConflictResolution;
import net.sf.xapp.objserver.types.Delta;

/**
 * This is the single threaded entry point handling requests to update and view a managed object
 */
public class ObjController extends AppAdaptor implements ObjManager {

    private final String key;
    private final MasterObject liveObject;
    private ObjTracker objTracker;

    public ObjController(String key, ObjectMeta rootObj) {
        this.key = key;
        liveObject = new MasterObject(rootObj);
    }

    public LiveObject getLiveObject() {
        return liveObject;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setCommChannel(CommChannel channel) {

        //wire up the comm channel as a listener on the public state
        liveObject.addListener(new ObjListenerAdaptor(getKey(), new BroadcastProxy<ObjListener, Void>(channel)));

        ObjManagerReply objManagerReply = new ObjManagerReplyAdaptor(getKey(), new NotifyProxy<ObjManagerReply>(channel));
        objTracker = new ObjTracker(liveObject, objManagerReply);
        liveObject.addListener(objTracker);
    }

    @Override
    public AppType getAppType() {
        return AppType.OBJ_SERVER;
    }

    @Override
    public void getObject(UserId principal) {
        objTracker.getObject(principal);
    }

    @Override
    public void getDeltas(UserId principal, Long revFrom, Long revTo) {
        objTracker.getDeltas(principal, revFrom, revTo);
    }

    @Override
    public void applyChanges(UserId principal, List<Delta> deltas, ConflictResolution conflictResolutionStrategy, Long baseRevision, Long localIdStart) {
        objTracker.applyChanges(principal, deltas, conflictResolutionStrategy, baseRevision, localIdStart);
    }
}
