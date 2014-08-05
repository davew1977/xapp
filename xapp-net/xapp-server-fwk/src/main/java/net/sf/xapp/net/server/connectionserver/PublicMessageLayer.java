/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.PublicEntryPoint;
import net.sf.xapp.net.server.channels.PlayerLocator;
import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.Message;
import ngpoker.common.types.PlayerId;
import ngpoker.common.types.PlayerLocation;
import net.sf.xapp.net.server.connectionserver.clientcontrol.to.SetInitialInfo;
import net.sf.xapp.net.server.connectionserver.listener.ConnectionListener;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import ngpoker.playerlookup.PlayerLookup;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicMessageLayer<T> implements MessageLayer<T, PlayerId>, MessageSender
{
    private Logger log = Logger.getLogger(getClass());
    private IOLayer<T, PlayerId> ioLayer;
    private Map<PlayerId, T> sessions;
    private final PublicEntryPoint publicEntryPoint;
    private final ConnectionListener connectionListener;
    private final NodeInfo nodeInfo;
    private final ClusterFacade clusterFacade;
    private final PlayerLocator playerLocator;
    private final PlayerLookup playerLookup;

    public PublicMessageLayer(PublicEntryPoint publicEntryPoint,
                              ConnectionListener connectionListener,
                              NodeInfo nodeInfo,
                              ClusterFacade clusterFacade,
                              PlayerLocator playerLocator,
                              PlayerLookup playerLookup)
    {
        this.publicEntryPoint = publicEntryPoint;
        this.connectionListener = connectionListener;
        this.nodeInfo = nodeInfo;
        this.clusterFacade = clusterFacade;
        this.playerLocator = playerLocator;
        this.playerLookup = playerLookup;
        sessions = new HashMap<PlayerId,T>();
    }

    @Override
    public void setIOLayer(IOLayer<T, PlayerId> ioLayer)
    {
        this.ioLayer = ioLayer;
    }

    @Override
    public void sessionOpened(T session)
    {
        PlayerId playerId = ioLayer.getSessionKey(session);
        sessions.put(playerId, session);
        clusterFacade.addPlayerLocationMapping(playerId, nodeInfo.getMyNodeId());
        connectionListener.playerConnected(playerId, nodeInfo.getMyNodeId());
        boolean guest = playerLookup.findPlayer(playerId).getPlayer().isGuest();

        //check for channels to join
        List<PlayerLocation> channels = playerLocator.getLocations(playerId);
        post(playerId, new SetInitialInfo(playerId, channels, System.currentTimeMillis(), guest));
    }

    @Override
    public void sessionClosed(T session)
    {
        PlayerId playerId = ioLayer.getSessionKey(session);
        sessions.remove(playerId);
        clusterFacade.removePlayerLocationMapping(playerId);
        connectionListener.playerDisconnected(playerId);
    }

    @Override
    public void handleMessage(T session, InMessage message)
    {
        log.debug("received: " + message);
        publicEntryPoint.handleMessage(message);
    }

    @Override
    public void broadcast(List<PlayerId> players, Message message)
    {
        for (PlayerId playerId : players)
        {
            post(playerId, message);
        }
    }

    @Override
    public void post(PlayerId playerId, Message message)
    {
        T session = sessions.get(playerId);
        if (session!=null)
        {
            ioLayer.sendMessage(session, message);
        }
        else
        {
            log.debug(String.format("trying to post message to player with no session: %s %s", playerId, message));
        }
    }
}
