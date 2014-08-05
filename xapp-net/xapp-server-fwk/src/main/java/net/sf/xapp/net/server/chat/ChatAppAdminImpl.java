/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.chat;

import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.PlayerLocator;
import ngpoker.client.channel.Channel;
import ngpoker.client.channel.ChannelAdaptor;
import net.sf.xapp.net.server.chat.admin.ChatAppAdmin;
import net.sf.xapp.net.server.chat.chatapp.ChatApp;
import net.sf.xapp.net.server.chat.chatapp.ChatAppAdaptor;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import ngpoker.common.types.Language;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import net.sf.xapp.net.server.lobby.internal.LobbyInternal;
import ngpoker.playerlookup.PlayerLookup;

public class ChatAppAdminImpl implements ChatAppAdmin
{
    private final EntityRepository entityRepository;
    private final ClusterFacade clusterFacade;
    private final PlayerLookup playerLookup;
    private final MessageSender messageSender;
    private final EventLoopManager eventLoopManager;
    private final PlayerLocator playerLocator;
    private final LobbyInternal lobbyInternal;

    public ChatAppAdminImpl(EntityRepository entityRepository,
                            ClusterFacade clusterFacade,
                            PlayerLookup playerLookup,
                            EventLoopManager eventLoopManager,
                            PlayerLocator playerLocator, MessageSender messageSender, LobbyInternal lobbyInternal)
    {
        this.entityRepository = entityRepository;
        this.clusterFacade = clusterFacade;
        this.playerLookup = playerLookup;
        this.eventLoopManager = eventLoopManager;
        this.playerLocator = playerLocator;
        this.messageSender = messageSender;
        this.lobbyInternal = lobbyInternal;
    }

    @Override
    public void create(String key, Language language, Integer maxOccupants, String name)
    {
        ChatAppImpl chatApp = new ChatAppImpl(playerLookup, key);
        ChannelImpl channel = new ChannelImpl(messageSender, playerLocator, chatApp);

        //event loop wrapper
        ChatApp chatAppEntity = new ChatAppAdaptor(key,
                new EventLoopMessageHandler<ChatApp>(eventLoopManager, chatApp));
        Channel channelEntity = new ChannelAdaptor(key,
                new EventLoopMessageHandler<Channel>(eventLoopManager, channel));

        entityRepository.add(ChatApp.class, key, chatAppEntity);
        entityRepository.add(Channel.class, key, channelEntity);

        clusterFacade.addEntityMapping(key);

       // lobbyInternal.entityAdded(key, new ChatroomInLobby(maxOccupants, language, key, name));
    }
}
