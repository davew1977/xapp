/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.chat;

import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatclient.ChatClientAdaptor;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.api.chatuser.ChatUserAdaptor;
import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.channels.App;
import net.sf.xapp.net.server.channels.BroadcastProxy;
import net.sf.xapp.net.server.channels.CommChannel;
import net.sf.xapp.net.server.channels.NotifyProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatAppImpl implements ChatApp, App
{
    private final String key;
    private ChatClient chatClient;
    private Map<UserId, String> nicknameMap;
    private UserLookup userLookup;
    private CommChannel commChannel;

    public ChatAppImpl(UserLookup userLookup, String key)
    {
        this.userLookup = userLookup;
        this.key = key;
        nicknameMap = new LinkedHashMap<UserId, String>();
    }

    public void setCommChannel(CommChannel commChannel)
    {
        this.commChannel = commChannel;
        chatClient = new ChatClientAdaptor(key, new BroadcastProxy<ChatClient, Void>(commChannel));
    }

    @Override
    public AppType getAppType()
    {
        return AppType.CHAT_ROOM;
    }
    @Override
    public void newChatMessage(UserId sender, String message)
    {
        chatClient.chatBroadcast(sender, message, nicknameMap.get(sender));
    }

    @Override
    public void userConnected(UserId userId)
    {
    }

    @Override
    public void userDisconnected(UserId userId)
    {
        userLeft(userId);
        commChannel.removeUser(userId);
    }

    @Override
    public void userJoined(UserId userId)
    {
        String nickname = userLookup.findUser(userId).getUser().getUsername();
        ChatUser chatPlayer = new ChatUserAdaptor(new NotifyProxy<ChatUser>(commChannel));
        nicknameMap.put(userId, nickname);
        chatClient.userJoined(userId, nickname);
        chatPlayer.chatChannelState(userId, nicknameMap);
    }

    @Override
    public void userLeft(UserId userId)
    {
        chatClient.userLeft(userId, nicknameMap.remove(userId));
    }

    @Override
    public String getKey()
    {
        return key;
    }
}

