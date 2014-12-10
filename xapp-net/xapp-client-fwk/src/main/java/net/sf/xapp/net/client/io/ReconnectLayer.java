/*
 *
 * Date: 2010-mar-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.framework.Logger;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.net.common.util.ThreadUtils;

/**
 */
public class ReconnectLayer implements Connectable, ConnectionListener {
    private final ServerProxy serverProxy;
    private ConnectionState connectionState;

    public ReconnectLayer(ServerProxy serverProxy) {
        this.serverProxy = serverProxy;
        serverProxy.addListener(this);
    }

    public boolean connect(boolean keepTrying) {
        boolean connected = serverProxy.connect(false);
        if (!connected && keepTrying) {
            serverProxy.setConnecting();
            connectWithRetry();
        }
        return connected;
    }

    @Override
    public void setConnecting() {
        throw new UnsupportedOperationException();//only the reconnect layer itself should call this
    }

    @Override
    public void setOffline() {
        serverProxy.setOffline();
    }

    @Override
    public void connectionStateChanged(ConnectionState newState) {
        connectionState = newState;
        switch (newState) {
            case ONLINE:
                break;
            case OFFLINE:
                break;
            case CONNECTING:
                break;
            case CONNECTION_LOST:
                connectWithRetry(); //pauses first
                break;
        }
    }

    private void connectWithRetry() {
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectionState!=ConnectionState.OFFLINE && !serverProxy.isConnected()) {
                    boolean connected = serverProxy.connect(false);
                    if (!connected) {
                        serverProxy.setConnecting();
                        connectWithRetry();
                    }
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
        System.out.println("attempting to connect");
    }

    @Override
    public void handleConnectException(Exception e) {
    }

    @Override
    public boolean isConnected() {
        return serverProxy.isConnected();
    }

    @Override
    public String toString() {
        return serverProxy.toString();
    }

}