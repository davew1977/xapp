/*
 *
 * Date: 2010-sep-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.lobby;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelAdaptor;
import net.sf.xapp.net.api.lobbyinternal.LobbyInternal;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManager;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManagerAdaptor;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.framework.memdb.StorableType;
import net.sf.xapp.net.server.repos.EntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class LobbyFactory
{
    private final Logger log = LoggerFactory.getLogger(getClass());
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
