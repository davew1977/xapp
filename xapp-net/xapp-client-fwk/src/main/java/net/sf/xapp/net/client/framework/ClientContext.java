package net.sf.xapp.net.client.framework;

/**
 * © Webatron Ltd
 * Created by dwebber
 */

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelAdaptor;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatapp.ChatAppAdaptor;
import net.sf.xapp.net.api.clientcontrol.ClientControl;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManager;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManagerAdaptor;
import net.sf.xapp.net.api.userapi.UserApi;
import net.sf.xapp.net.api.userapi.UserApiAdaptor;
import net.sf.xapp.net.api.userentity.UserEntity;
import net.sf.xapp.net.api.userentity.UserEntityListener;
import net.sf.xapp.net.client.app.ChannelJoiner;
import net.sf.xapp.net.client.io.Connectable;
import net.sf.xapp.net.client.io.ConnectionListener;
import net.sf.xapp.net.client.io.ReconnectLayer;
import net.sf.xapp.net.client.io.ServerProxy;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.NullMessageHandler;
import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserLocation;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientContext implements MessageHandler, Connectable, ClientControl, ConnectionListener {
    private final Logger log = Logger.getLogger(getClass());
    private Connectable server;
    private UserId userId;
    private MessageHandler serverBoundMessageHandler;
    private MessageDispatcher messageDispatcher;
    private String previousUsername;
    private ChannelJoiner channelJoiner;
    private UserEntity user;
    private final Set<String> currentlyLoadingKeys;
    private long serverTime;
    private long initTime;
    private boolean guest;
    private ConnectionState connectionState;

    public ClientContext() {
        this(null, new NullMessageHandler());
    }

    public ClientContext(MessageHandler serverProxy) {
        this(null, serverProxy);
    }

    public ClientContext(String userId) {
        this(userId, new NullMessageHandler());
    }

    public ClientContext(String userId, MessageHandler serverProxy) {
        this.userId = userId != null ? new UserId(userId) : null;
        messageDispatcher = new MessageDispatcher();
        currentlyLoadingKeys = new HashSet<String>();
        wire(ClientControl.class, this);

        this.serverBoundMessageHandler = serverProxy;

        if (serverProxy instanceof ServerProxy) {
            ServerProxy proxy = (ServerProxy) serverProxy;
            proxy.setClient(this);
            server = new ReconnectLayer(proxy);
        } else {
        }

        addConnectionListener(this);
    }

    private <T> T create(String className) {
        try {
            return (T) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean connect() {
        if (server == null) {
            throw new RuntimeException("no server to connect to");
        }
        return server.connect();
    }

    @Override
    public void setConnecting() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOffline() {
        if (server != null) {
            //clear all state
            messageDispatcher.clearStatefulHandlers();
            server.setOffline();
        } else {
            log.info("no server to disconnect from");
        }
    }

    public void setReconnect(boolean reconnect) {
        ((ReconnectLayer) server).setReconnect(reconnect);
    }

    @Override
    public boolean isConnected() {
        return server.isConnected();
    }

    public UserId getUserId() {
        return userId;
    }

    public MessageHandler getServerBoundMessageHandler() {
        return serverBoundMessageHandler;
    }

    @Override
    public Object handleMessage(final InMessage inMessage) {
        /**
         * always perform all tasks on the event dispatch thread
         */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageDispatcher.handleMessage(inMessage);
            }
        });
        return null;
    }

    public String username() {
        return user != null ? user.getUserInfo().getNickname() : previousUsername;
    }

    public void setPreviousUsername(String previousUsername) {
        this.previousUsername = previousUsername;
    }

    public void setChannelJoiner(ChannelJoiner channelJoiner) {
        this.channelJoiner = channelJoiner;
    }

    public ChannelJoiner getChannelJoiner() {
        return channelJoiner;
    }

    public UserEntity getUser() {
        return user;
    }

    /**
     * route all messages of given type on given context to impl
     *
     * @param aClass
     * @param key
     * @param impl
     * @param <T>
     */
    public <T> void wire(Class<T> aClass, String key, T impl) {
        assert aClass.isInterface();
        messageDispatcher.addDelegate(aClass, key, impl);
    }

    public <T> void wire(Class<T> aClass, T impl) {
        assert aClass.isInterface();
        messageDispatcher.addDelegate(aClass, impl);
    }

    public void unwireAll(String entityKey) {
        messageDispatcher.removeAllForKey(entityKey);
    }

    public <T> Channel joinChannel(String key, Class<T> channelClientClass, T impl) {
        messageDispatcher.removeAllForKey(key);
        wire(channelClientClass, key, impl);
        Channel channel = channel(key);
        channel.join(getUserId());
        return channel;
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (serverBoundMessageHandler instanceof ServerProxy) {
            ServerProxy serverProxy = (ServerProxy) serverBoundMessageHandler;
            serverProxy.addListener(listener);
        } else {
            log.info("no server proxy to add listener to");
        }
    }

    @Override
    public void setInitialInfo(UserId principal, List<UserLocation> locations, Long serverTime, Boolean guest) {
        this.serverTime = serverTime;
        this.initTime = System.currentTimeMillis();
        this.guest = guest;
    }

    public long serverTimeMillis() {
        return serverTime + (System.currentTimeMillis() - initTime);
    }

    @Override
    public void forceJoinChannel(UserId principal, String channelId, AppType appType) {

    }

    @Override
    public void setUser(UserId principal, UserEntity user) {
        assert principal == null || userId.equals(principal);
        log.debug(user);
        this.user = user;
        this.userId = principal;
        wire(UserEntityListener.class, user);

    }

    public LobbySessionManager lobbySessionManager(String lobbyKey) {
        return new LobbySessionManagerAdaptor(lobbyKey, serverBoundMessageHandler);
    }

    public UserApi userApi() {
        return new UserApiAdaptor(serverBoundMessageHandler);
    }

    public Channel channel(String key) {
        return new ChannelAdaptor(key, serverBoundMessageHandler);
    }

    public ChatApp chatApp(String key) {
        return new ChatAppAdaptor(key, serverBoundMessageHandler);
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void login() {
        login(false);
    }

    public void login(boolean bot) {
        userApi().loginWithToken(getUserId(), "foo", bot);
    }

    public void addGenericHandler(MessageHandler messageHandler) {
        messageDispatcher.addGenericHandler(messageHandler);
    }

    public boolean isGuest() {
        return guest;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void connectionStateChanged(ConnectionState newState) {
        this.connectionState = newState;
    }

    @Override
    public void handleConnectException(Exception e) {

    }

    public boolean isOnlineMode() {
        return connectionState != ConnectionState.OFFLINE;
    }
}
