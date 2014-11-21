/*
 *
 * Date: 2010-mar-05
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;


import javax.swing.*;

import net.sf.xapp.Global;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.TransportObject;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ServerProxyImpl implements ServerProxy {
    private final List<ConnectionListener> listeners;
    private final String host;
    private final int port;
    private MessageHandler client;
    private DataOutputStream out;
    private Socket socket;
    private boolean wasSetOffline;

    public ServerProxyImpl(HostInfo hostInfo) {
        host = hostInfo.host;
        port = hostInfo.port;
        listeners = new ArrayList<ConnectionListener>();
    }

    public void setClient(MessageHandler client) {
        this.client = client;
    }

    @Override
    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setConnecting() {
        for (ConnectionListener listener : listeners) {
            listener.connectionStateChanged(ConnectionState.CONNECTING);
        }
    }

    @Override
    public void setOffline() {
        wasSetOffline = disconnect();
        for (ConnectionListener listener : listeners) {
            listener.connectionStateChanged(ConnectionState.OFFLINE);
        }
    }

    public boolean connect() {
        disconnect();
        wasSetOffline = false;
        final DataInputStream dis;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            dis = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            for (ConnectionListener listener : listeners) {
                listener.handleConnectException(e);
            }
            return false;
        }
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try{
                        int messageSize = dis.readInt();
                        int messageType = dis.readInt();
                        final TransportObject message = Global.create(messageType);
                        message.readData(dis);
                        client.handleMessage((InMessage) message);
                        System.out.println(StringUtils.pad(messageSize + " bytes", false, 16) + StringUtils.truncate(String.valueOf(message), 256));
                    } catch (SocketException e) {
                        socket = null;
                        if (e.getMessage().equals("Connection reset") ||
                                e.getMessage().equalsIgnoreCase("Socket closed")) {
                            if (!wasSetOffline) {
                                notifyDisconnected();
                            }
                            return;
                        } else {
                            throw new RuntimeException(e);
                        }
                    } catch (EOFException e) {
                        socket = null;
                        notifyDisconnected();
                        return;
                    } catch (IOException e) {
                        socket = null;
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        for (ConnectionListener listener : listeners) {
            listener.connectionStateChanged(ConnectionState.ONLINE);
        }
        return true;
    }

    private void notifyDisconnected() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (ConnectionListener listener : listeners) {
                    listener.connectionStateChanged(ConnectionState.CONNECTION_LOST);
                }
            }
        });
    }

    private boolean disconnect() {
        if (isConnected()) {
            try {
                socket.close();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", host, port);
    }

    @Override
    public Object handleMessage(InMessage message) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeData(new DataOutputStream(baos));
            out.writeInt(baos.size() + 4);
            out.writeInt(message.type().getId());
            out.write(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}