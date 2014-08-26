/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.api.clientcontrol.to.SetInitialInfo;
import net.sf.xapp.net.api.connectionlistener.ConnectionListener;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.Message;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserLocation;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.PublicEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicMessageLayer<T> implements MessageLayer<T, UserId>, MessageSender
{
    private Logger log = LoggerFactory.getLogger(getClass());
    private IOLayer<T, UserId> ioLayer;
    private Map<UserId, T> sessions;
    private final PublicEntryPoint publicEntryPoint;
    private final ConnectionListener connectionListener;
    private final NodeInfo nodeInfo;
    private final ClusterFacade clusterFacade;
    private final UserLocator userLocator;
    private final UserLookup userLookup;

    public PublicMessageLayer(PublicEntryPoint publicEntryPoint,
                              ConnectionListener connectionListener,
                              NodeInfo nodeInfo,
                              ClusterFacade clusterFacade,
                              UserLocator userLocator,
                              UserLookup userLookup)
    {
        this.publicEntryPoint = publicEntryPoint;
        this.connectionListener = connectionListener;
        this.nodeInfo = nodeInfo;
        this.clusterFacade = clusterFacade;
        this.userLocator = userLocator;
        this.userLookup = userLookup;
        sessions = new HashMap<UserId,T>();
    }

    @Override
    public void setIOLayer(IOLayer<T, UserId> ioLayer)
    {
        this.ioLayer = ioLayer;
    }

    @Override
    public void sessionOpened(T session)
    {
        UserId userId = ioLayer.getSessionKey(session);
        sessions.put(userId, session);
        clusterFacade.addUserLocationMapping(userId, nodeInfo.getMyNodeId());
        connectionListener.userConnected(userId, nodeInfo.getMyNodeId());
        boolean guest = userLookup.findUser(userId).getUser().isGuest();

        //check for channels to join
        List<UserLocation> channels = userLocator.getLocations(userId);
        post(userId, new SetInitialInfo(userId, channels, System.currentTimeMillis(), guest));
    }

    @Override
    public void sessionClosed(T session)
    {
        UserId userId = ioLayer.getSessionKey(session);
        sessions.remove(userId);
        clusterFacade.removeUserLocationMapping(userId);
        connectionListener.userDisconnected(userId);
    }

    @Override
    public void handleMessage(T session, InMessage message)
    {
        log.debug("received: " + message);
        publicEntryPoint.handleMessage(message);
    }

    @Override
    public void broadcast(List<UserId> users, Message message)
    {
        for (UserId userId : users)
        {
            post(userId, message);
        }
    }

    @Override
    public void post(UserId userId, Message message)
    {
        T session = sessions.get(userId);
        if (session!=null)
        {
            ioLayer.sendMessage(session, message);
        }
        else
        {
            log.debug(String.format("trying to post message to user with no session: %s %s", userId, message));
        }
    }
}
