/*
 *
 * Date: 2011-jan-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.ConnectionListener;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.client.io.ServerProxy;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.NullMessageHandler;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.net.connectionserver.mina.BytePacketCodecFactory;
import org.apache.mina.common.*;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MinaServerProxy extends IoHandlerAdapter implements ServerProxy
{
    private final List<ConnectionListener> listeners;
    private final String host;
    private final int port;
    private final SocketConnector connector;
    private MessageHandler client;
    private IoSession session;

    public MinaServerProxy(HostInfo hostInfo)
    {
        this(hostInfo, createSocketConnector());
    }

    public MinaServerProxy(HostInfo hostInfo, SocketConnector socketConnector)
    {
        listeners = new ArrayList<ConnectionListener>();
        this.host = hostInfo.host;
        this.port = hostInfo.port;
        this.connector = socketConnector;

        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        client = new NullMessageHandler();
    }

    @Override
    public void setConnecting() {
        for (ConnectionListener listener : listeners) {
            listener.connectionStateChanged(ConnectionState.CONNECTING);
        }
    }

    @Override
    public void setOffline() {
        disconnect();
        for (ConnectionListener listener : listeners) {
            listener.connectionStateChanged(ConnectionState.OFFLINE);
        }
    }

    public void setClient(MessageHandler client)
    {
        this.client = client;
    }

    public static SocketConnector createSocketConnector()
    {
        SocketConnector connector = new SocketConnector();
        IoFilter LOGGING_FILTER = new LoggingFilter();

        IoFilter CODEC_FILTER = new ProtocolCodecFilter(new BytePacketCodecFactory(false))
        {
            @Override
            public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception
            {
                super.exceptionCaught(nextFilter, session, cause);
                cause.printStackTrace();
            }
        };

        connector.getFilterChain().addLast("codec", CODEC_FILTER);
        connector.getFilterChain().addLast("logger", LOGGING_FILTER);

        return connector;
    }

    @Override
    public void addListener(ConnectionListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public Object handleMessage(InMessage message)
    {
        if (session!=null)
        {
            return session.write(message);
        }
        return null;
    }

    @Override
    public boolean connect(boolean keepTrying)
    {
        if (session != null && session.isConnected())
        {
            throw new IllegalStateException(
                    "Already connected. Disconnect first.");
        }

        try
        {
            InetSocketAddress address = new InetSocketAddress(host, port);
            ConnectFuture future = connector.connect(address, this);
            future.join();
            if (!future.isConnected())
            {
                return false;
            }
            session = future.getSession();
            for (ConnectionListener listener : listeners)
            {
                listener.connectionStateChanged(ConnectionState.ONLINE);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    private void disconnect()
    {
        session.close();
    }

    @Override
    public void messageReceived(IoSession session, final Object message) throws Exception
    {
        /*InMessage m = (InMessage) message;
        System.out.println(m.messageType() + " " + TransportHelper.toByteArray(m).length);*/
        client.handleMessage((InMessage) message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception
    {
        for (ConnectionListener listener : listeners)
        {
            listener.connectionStateChanged(ConnectionState.CONNECTION_LOST);
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception
    {
    }

    public boolean isConnected()
    {
        return session!=null && session.isConnected();
    }

    @Override
    public String toString()
    {
        return String.format("%s:%s", host, port);
    }
}
