/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.chat;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelAdaptor;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatapp.ChatAppAdaptor;
import net.sf.xapp.net.api.chatappadmin.ChatAppAdmin;
import net.sf.xapp.net.api.lobbyinternal.LobbyInternal;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.common.types.Language;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.repos.EntityRepository;

public class ChatAppAdminImpl implements ChatAppAdmin
{
    private final EntityRepository entityRepository;
    private final ClusterFacade clusterFacade;
    private final UserLookup userLookup;
    private final MessageSender messageSender;
    private final EventLoopManager eventLoopManager;
    private final UserLocator userLocator;
    private final LobbyInternal lobbyInternal;

    public ChatAppAdminImpl(EntityRepository entityRepository,
                            ClusterFacade clusterFacade,
                            UserLookup userLookup,
                            EventLoopManager eventLoopManager,
                            UserLocator userLocator, MessageSender messageSender, LobbyInternal lobbyInternal)
    {
        this.entityRepository = entityRepository;
        this.clusterFacade = clusterFacade;
        this.userLookup = userLookup;
        this.eventLoopManager = eventLoopManager;
        this.userLocator = userLocator;
        this.messageSender = messageSender;
        this.lobbyInternal = lobbyInternal;
    }

    @Override
    public void create(String key, Language language, Integer maxOccupants, String name)
    {
        ChatAppImpl chatApp = new ChatAppImpl(userLookup, key);
        ChannelImpl channel = new ChannelImpl(messageSender, userLocator, chatApp);

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
