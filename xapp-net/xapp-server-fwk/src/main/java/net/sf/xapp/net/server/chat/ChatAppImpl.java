/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.chat;

import net.sf.xapp.net.server.channels.BroadcastProxy;
import net.sf.xapp.net.server.channels.CommChannel;
import net.sf.xapp.net.server.channels.NotifyProxy;
import net.sf.xapp.net.server.channels.App;
import net.sf.xapp.net.server.chat.chatapp.ChatApp;
import net.sf.xapp.net.server.chat.client.ChatClient;
import net.sf.xapp.net.server.chat.client.ChatClientAdaptor;
import net.sf.xapp.net.server.chat.player.ChatPlayer;
import net.sf.xapp.net.server.chat.player.ChatPlayerAdaptor;
import ngpoker.common.types.AppType;
import ngpoker.common.types.PlayerId;
import ngpoker.playerlookup.PlayerLookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChatAppImpl implements ChatApp, App
{
    private final String key;
    private ChatClient chatClient;
    private Map<PlayerId, ChatUser> nicknameMap;
    private PlayerLookup playerLookup;
    private CommChannel commChannel;

    public ChatAppImpl(PlayerLookup playerLookup, String key)
    {
        this.playerLookup = playerLookup;
        this.key = key;
        nicknameMap = new HashMap<PlayerId, ChatUser>();
    }

    public void setCommChannel(CommChannel commChannel)
    {
        this.commChannel = commChannel;
        chatClient = new ChatClientAdaptor(key, new BroadcastProxy<ChatClient, Void>(commChannel));
    }

    @Override
    public AppType getAppType()
    {
        return AppType.CHAT;
    }

    @Override
    public void newChatMessage(PlayerId sender, String message)
    {
        chatClient.chatBroadcast(sender, message, nicknameMap.get(sender).nickname);
    }

    @Override
    public void playerConnected(PlayerId playerId)
    {
    }

    @Override
    public void playerDisconnected(PlayerId playerId)
    {
        playerLeft(playerId);
        commChannel.removePlayer(playerId);
    }

    @Override
    public void playerJoined(PlayerId playerId)
    {
        String nickname = playerLookup.findPlayer(playerId).getPlayer().getUsername();
        ChatPlayer chatPlayer = new ChatPlayerAdaptor(new NotifyProxy<ChatPlayer>(commChannel));
        nicknameMap.put(playerId, new ChatUser(nickname, chatPlayer));
        chatClient.playerJoined(nickname);
        Collection<ChatUser> chatUsers = nicknameMap.values();
        ArrayList<String> usernames = new ArrayList<String>();
        for (ChatUser chatUser : chatUsers)
        {
            usernames.add(chatUser.nickname);
        }
        chatPlayer.chatChannelState(playerId, usernames);
    }

    @Override
    public void playerLeft(PlayerId playerId)
    {
        chatClient.playerLeft(nicknameMap.remove(playerId).nickname);
    }

    private static class ChatUser
    {
        private final String nickname;
        private final ChatPlayer chatPlayer;

        private ChatUser(String nickname, ChatPlayer chatPlayer)
        {
            this.nickname = nickname;
            this.chatPlayer = chatPlayer;
        }
    }

    @Override
    public String getKey()
    {
        return key;
    }
}

