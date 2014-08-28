package net.sf.xapp.objclient;

import net.sf.xapp.net.client.framework.ClientContext;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerAdaptor;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class ObjClientContext extends ClientContext {
    public ObjClientContext(String userId, MessageHandler serverProxy) {
        super(userId, serverProxy);
    }

    public ObjManager objManager(String key) {
        return new ObjManagerAdaptor(key, getServerBoundMessageHandler());
    }
}
