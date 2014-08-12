/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.client.channel.Channel;
import ngpoker.client.channel.ChannelReply;
import ngpoker.client.channel.ChannelReplyAdaptor;
import net.sf.xapp.net.common.framework.Message;
import ngpoker.common.types.ErrorCode;
import ngpoker.common.types.GenericException;
import net.sf.xapp.net.common.types.UserId;
import ngpoker.common.types.PlayerLocation;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;

import java.util.ArrayList;
import java.util.List;

public class ChannelImpl implements Channel, CommChannel
{
    private final String key;
    private final List<UserId> userIds;
    private final MessageSender messageSender;
    private final App masterApp;
    private final App[] otherApps;
    private final PlayerLocator playerLocator;
    private final ChannelReply channelReply;

    public ChannelImpl(MessageSender messageSender, PlayerLocator playerLocator, App masterApp, App... apps)
    {
        this.playerLocator = playerLocator;
        this.key = masterApp.getKey();
        this.userIds = new ArrayList<UserId>();
        this.messageSender = messageSender;
        this.masterApp = masterApp;
        playerLocator.registerLocation(new PlayerLocation(key, masterApp.getAppType()));
        channelReply = new ChannelReplyAdaptor(key, new NotifyProxy<ChannelReply>(this));
        otherApps = apps;
        masterApp.setCommChannel(this);
        for (App app : apps)
        {
            app.setCommChannel(this);//app gets raw channel instance, because it is already sync'd on the same key
        }

    }

    @Override
    public void join(UserId userId)
    {
        addPlayer(userId);
        masterApp.playerJoined(userId);
        for (App otherApp : otherApps)
        {
            otherApp.playerJoined(userId);
        }
        channelReply.joinResponse(userId, null);
    }

    public void addPlayer(UserId userId)
    {
        if (!userIds.contains(userId))
        {
            //throw new GenericException(ErrorCode.PLAYER_ALREADY_JOINED);
            userIds.add(userId);
        }
        playerLocator.addMapping(userId, key);
    }

    @Override
    public void leave(UserId userId)
    {
        masterApp.playerLeft(userId);

        for (App otherApp : otherApps)
        {
            otherApp.playerLeft(userId);
        }
        removePlayer(userId);
        channelReply.leaveResponse(userId, null);
    }

    @Override
    public void removePlayer(UserId userId)
    {
        boolean removed = userIds.remove(userId);
        if (!removed)
        {
            throw new GenericException(ErrorCode.PLAYER_NOT_JOINED);
        }
        playerLocator.removeMapping(userId, key);
    }

    @Override
    public void playerConnected(UserId userId)
    {
        masterApp.playerConnected(userId);
        for (App otherApp : otherApps)
        {
            otherApp.playerConnected(userId);
        }
    }

    @Override
    public void playerDisconnected(UserId userId)
    {
        masterApp.playerDisconnected(userId);

        for (App otherApp : otherApps)
        {
            otherApp.playerDisconnected(userId);
        }
    }

    @Override
    public void broadcast(Message message)
    {
        messageSender.broadcast(userIds, message);
    }

    @Override
    public void send(UserId userId, Message message)
    {
        messageSender.post(userId, message);
    }

    public boolean isPlayerJoined(UserId userId)
    {
        return userIds.contains(userId);
    }
}
