/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelReply;
import net.sf.xapp.net.api.channel.ChannelReplyAdaptor;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.common.framework.Message;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserLocation;

import java.util.ArrayList;
import java.util.List;

public class ChannelImpl implements Channel, CommChannel
{
    private final String key;
    private final List<UserId> userIds;
    private final MessageSender messageSender;
    private final App masterApp;
    private final App[] otherApps;
    private final UserLocator userLocator;
    private final ChannelReply channelReply;

    public ChannelImpl(MessageSender messageSender, UserLocator userLocator, App masterApp, App... apps)
    {
        this.userLocator = userLocator;
        this.key = masterApp.getKey();
        this.userIds = new ArrayList<UserId>();
        this.messageSender = messageSender;
        this.masterApp = masterApp;
        userLocator.registerLocation(new UserLocation(key, masterApp.getAppType()));
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
        addUser(userId);
        masterApp.userJoined(userId);
        for (App otherApp : otherApps)
        {
            otherApp.userJoined(userId);
        }
        channelReply.joinResponse(userId, null);
    }

    public void addUser(UserId userId)
    {
        if (!userIds.contains(userId))
        {
            //throw new GenericException(ErrorCode.PLAYER_ALREADY_JOINED);
            userIds.add(userId);
        }
        userLocator.addMapping(userId, key);
    }

    @Override
    public void leave(UserId userId)
    {
        masterApp.userLeft(userId);

        for (App otherApp : otherApps)
        {
            otherApp.userLeft(userId);
        }
        removeUser(userId);
        channelReply.leaveResponse(userId, null);
    }

    @Override
    public void removeUser(UserId userId)
    {
        boolean removed = userIds.remove(userId);
        if (!removed)
        {
            throw new GenericException(ErrorCode.PLAYER_NOT_JOINED);
        }
        userLocator.removeMapping(userId, key);
    }

    @Override
    public void userConnected(UserId userId)
    {
        masterApp.userConnected(userId);
        for (App otherApp : otherApps)
        {
            otherApp.userConnected(userId);
        }
    }

    @Override
    public void userDisconnected(UserId userId)
    {
        masterApp.userDisconnected(userId);

        for (App otherApp : otherApps)
        {
            otherApp.userDisconnected(userId);
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
