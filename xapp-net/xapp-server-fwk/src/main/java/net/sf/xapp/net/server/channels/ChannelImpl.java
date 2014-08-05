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
import ngpoker.common.framework.Message;
import ngpoker.common.types.ErrorCode;
import ngpoker.common.types.GenericException;
import ngpoker.common.types.PlayerId;
import ngpoker.common.types.PlayerLocation;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;

import java.util.ArrayList;
import java.util.List;

public class ChannelImpl implements Channel, CommChannel
{
    private final String key;
    private final List<PlayerId> playerIds;
    private final MessageSender messageSender;
    private final App masterApp;
    private final App[] otherApps;
    private final PlayerLocator playerLocator;
    private final ChannelReply channelReply;

    public ChannelImpl(MessageSender messageSender, PlayerLocator playerLocator, App masterApp, App... apps)
    {
        this.playerLocator = playerLocator;
        this.key = masterApp.getKey();
        this.playerIds = new ArrayList<PlayerId>();
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
    public void join(PlayerId playerId)
    {
        addPlayer(playerId);
        masterApp.playerJoined(playerId);
        for (App otherApp : otherApps)
        {
            otherApp.playerJoined(playerId);
        }
        channelReply.joinResponse(playerId, null);
    }

    public void addPlayer(PlayerId playerId)
    {
        if (!playerIds.contains(playerId))
        {
            //throw new GenericException(ErrorCode.PLAYER_ALREADY_JOINED);
            playerIds.add(playerId);
        }
        playerLocator.addMapping(playerId, key);
    }

    @Override
    public void leave(PlayerId playerId)
    {
        masterApp.playerLeft(playerId);

        for (App otherApp : otherApps)
        {
            otherApp.playerLeft(playerId);
        }
        removePlayer(playerId);
        channelReply.leaveResponse(playerId, null);
    }

    @Override
    public void removePlayer(PlayerId playerId)
    {
        boolean removed = playerIds.remove(playerId);
        if (!removed)
        {
            throw new GenericException(ErrorCode.PLAYER_NOT_JOINED);
        }
        playerLocator.removeMapping(playerId, key);
    }

    @Override
    public void playerConnected(PlayerId playerId)
    {
        masterApp.playerConnected(playerId);
        for (App otherApp : otherApps)
        {
            otherApp.playerConnected(playerId);
        }
    }

    @Override
    public void playerDisconnected(PlayerId playerId)
    {
        masterApp.playerDisconnected(playerId);

        for (App otherApp : otherApps)
        {
            otherApp.playerDisconnected(playerId);
        }
    }

    @Override
    public void broadcast(Message message)
    {
        messageSender.broadcast(playerIds, message);
    }

    @Override
    public void send(PlayerId playerId, Message message)
    {
        messageSender.post(playerId, message);
    }

    public boolean isPlayerJoined(PlayerId playerId)
    {
        return playerIds.contains(playerId);
    }
}
