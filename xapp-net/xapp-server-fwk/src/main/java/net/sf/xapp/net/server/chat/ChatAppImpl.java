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
import java.util.Map;

public class ChatAppImpl implements ChatApp, App
{
    private final String key;
    private ChatClient chatClient;
    private Map<UserId, UserEndpoint> nicknameMap;
    private UserLookup userLookup;
    private CommChannel commChannel;

    public ChatAppImpl(UserLookup userLookup, String key)
    {
        this.userLookup = userLookup;
        this.key = key;
        nicknameMap = new HashMap<UserId, UserEndpoint>();
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
        chatClient.chatBroadcast(sender, message, nicknameMap.get(sender).nickname);
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
        nicknameMap.put(userId, new UserEndpoint(nickname, chatPlayer));
        chatClient.userJoined(nickname);
        Collection<UserEndpoint> chatUsers = nicknameMap.values();
        ArrayList<String> usernames = new ArrayList<String>();
        for (UserEndpoint user : chatUsers)
        {
            usernames.add(user.nickname);
        }
        chatPlayer.chatChannelState(userId, usernames);
    }

    @Override
    public void userLeft(UserId userId)
    {
        chatClient.userLeft(nicknameMap.remove(userId).nickname);
    }

    private static class UserEndpoint
    {
        private final String nickname;
        private final ChatUser chatUser;

        private UserEndpoint(String nickname, ChatUser chatUser)
        {
            this.nickname = nickname;
            this.chatUser = chatUser;
        }
    }

    @Override
    public String getKey()
    {
        return key;
    }
}

