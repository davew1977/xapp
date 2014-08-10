/*
 *
 * Date: 2010-mar-05
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;


import net.sf.xapp.utils.StringUtils;
import ng.Global;
import net.sf.xapp.net.common.framework.InMessage;
import ngpoker.common.framework.Message;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.framework.TransportObject;
import ngpoker.common.types.MessageTypeEnum;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ServerProxyImpl implements ServerProxy
{
    private final List<ConnectionListener> listeners;
    private final String host;
    private final int port;
    private MessageHandler client;
    private DataOutputStream out;
    private Socket socket;

    public ServerProxyImpl(HostInfo hostInfo)
    {
        host = hostInfo.host;
        port = hostInfo.port;
        listeners = new ArrayList<ConnectionListener>();
    }

    public void setClient(MessageHandler client)
    {
        this.client = client;
    }

    @Override
    public void addListener(ConnectionListener listener)
    {
        listeners.add(listener);
    }

    public boolean connect()
    {
        disconnect();
        final DataInputStream dis;
        try
        {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            dis = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            for (ConnectionListener listener : listeners)
            {
                listener.handleConnectException(e);
            }
            return false;
        }
        new Thread(new Runnable()
        {
            public void run()
            {
                while(true)
                {
                    try
                    {
                        int messageSize = dis.readInt();
                        int messageType = dis.readInt();
                        final TransportObject message = Global.create(messageType);
                        message.readData(dis);
                        client.handleMessage((InMessage) message);
                        System.out.println(StringUtils.pad(messageSize+" bytes", false, 16)  + message);
                    }
                    catch(SocketException e)
                    {
                        if(e.getMessage().equals("Connection reset"))
                        {
                            for (ConnectionListener listener : listeners)
                            {
                                listener.disconnected();
                            }
                            return;
                        }
                        else
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        for (ConnectionListener listener : listeners)
        {
            listener.connected();
        }
        return true;
    }

    public void disconnect()
    {
        if (socket !=null && socket.isConnected())
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        return socket!=null && socket.isConnected();
    }

    @Override
    public String toString()
    {
        return String.format("%s:%s", host, port);
    }

    @Override
    public Object handleMessage(InMessage message)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeData(new DataOutputStream(baos));
            out.writeInt(baos.size() + 4);
            out.writeInt(message.type().getId());
            out.write(baos.toByteArray());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

}