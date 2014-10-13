package net.sf.xapp.objclient;

import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatapp.ChatAppAdaptor;
import net.sf.xapp.net.client.framework.ClientContext;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class ObjClientContext extends ClientContext {
    public ObjClientContext(String userId, MessageHandler serverProxy) {
        super(userId, serverProxy);
    }

    public ObjManager objManager(String key) {
        return new ObjManagerAdaptor(key, getServerBoundMessageHandler());
    }

    public ObjUpdate objUpdate(String key) {
        return new ObjUpdateAdaptor(key, getServerBoundMessageHandler());
    }

    public ChatApp chatApp(String key) {
        return new ChatAppAdaptor(key, getServerBoundMessageHandler());
    }
}
