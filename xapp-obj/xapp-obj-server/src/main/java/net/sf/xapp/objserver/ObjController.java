package net.sf.xapp.objserver;

import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.channels.App;
import net.sf.xapp.net.server.channels.BroadcastProxy;
import net.sf.xapp.net.server.channels.CommChannel;
import net.sf.xapp.net.server.channels.NotifyProxy;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReplyAdaptor;
import net.sf.xapp.objserver.types.XmlObj;

/**
 * This is the single threaded entry point handling requests to update and view a managed object
 */
public class ObjController implements App, ObjManager {
    private CommChannel commChannel;
    private ObjListener objListener;
    private ObjManagerReply objManagerReply;

    private final String key;
    private final ObjectMeta rootObj;
    private final LiveObject liveObject;

    private long revision;

    public ObjController(String key, ObjectMeta rootObj) {
        this.key = key;
        this.rootObj = rootObj;
        liveObject = new LiveObject(rootObj);
    }

    @Override
    public void userConnected(UserId userId) {

    }

    @Override
    public void userDisconnected(UserId userId) {

    }

    @Override
    public void userJoined(UserId userId) {

    }

    @Override
    public void userLeft(UserId userId) {

    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setCommChannel(CommChannel channel) {

        this.commChannel = channel;
        //wire up the comm channel as a listener on the public state
        liveObject.setListener(new ObjListenerAdaptor(getKey(), new BroadcastProxy<ObjListener, Void>(commChannel)));
        objManagerReply = new ObjManagerReplyAdaptor(getKey(), new NotifyProxy<ObjManagerReply>(commChannel));
    }

    @Override
    public AppType getAppType() {
        return AppType.OBJ_SERVER;
    }

    @Override
    public void getObject(UserId principal) {
        //todo cache xml at this revision
        objManagerReply.getObjectResponse(principal, new XmlObj(rootObj.getType(), rootObj.toXml(), revision, rootObj.getId()), null);
    }

    @Override
    public void getDeltas(UserId principal, Long revFrom, Long revTo) {

    }
}
