/*
 *
 * Date: 2010-sep-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.lobby;

import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import ngpoker.client.channel.Channel;
import ngpoker.client.channel.ChannelAdaptor;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import net.sf.xapp.net.server.lobby.internal.LobbyInternal;
import net.sf.xapp.net.server.lobby.session.LobbySessionManager;
import net.sf.xapp.net.server.lobby.session.LobbySessionManagerAdaptor;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.framework.memdb.StorableType;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;

public class LobbyFactory
{
    private final Logger log = Logger.getLogger(getClass());
    private final EntityRepository entityRepository;
    private final ClusterFacade clusterFacade;
    private final MessageSender messageSender;
    private final EventLoopManager eventLoopManager;
    private final UserLocator userLocator;
    private final StorableType storableType;
    private final String key;
    private final String lobbyName;

    public LobbyFactory(String lobbyName,
                        EntityRepository entityRepository,
                        ClusterFacade clusterFacade,
                        MessageSender messageSender,
                        EventLoopManager eventLoopManager,
                        UserLocator userLocator,
                        StorableType storableType, NodeInfo nodeInfo)
    {
        this.entityRepository = entityRepository;
        this.clusterFacade = clusterFacade;
        this.messageSender = messageSender;
        this.eventLoopManager = eventLoopManager;
        this.userLocator = userLocator;
        this.storableType = storableType;
        this.key = lobbyName + "_" + nodeInfo.getMyNodeId().getValue();
        this.lobbyName = lobbyName;
    }

    @PostConstruct
    public void init()
    {
        log.info("creating " + lobbyName);
        LobbySessionManagerImpl lobby = new LobbySessionManagerImpl(key, storableType);
        ChannelImpl channel = new ChannelImpl(messageSender, userLocator, lobby);

        //event loop wrapper
        LobbySessionManager lobbySessionManagerEntity = new LobbySessionManagerAdaptor( key,
                new EventLoopMessageHandler<LobbySessionManager>(eventLoopManager, lobby));
        Channel channelEntity = new ChannelAdaptor(key,
                new EventLoopMessageHandler<Channel>(eventLoopManager, channel));

        
        entityRepository.add(LobbySessionManager.class, key, lobbySessionManagerEntity);
        entityRepository.add(Channel.class, key, channelEntity);

        clusterFacade.addEntityMapping(key);

        clusterFacade.addTopicListener(lobbyName,
                new EventLoopMessageHandler<LobbyInternal>(eventLoopManager, lobby, key));

    }
}
