/*
 *
 * Date: 2010-mar-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

import ngpoker.common.util.ThreadUtils;

/**
 */
public class ReconnectLayer implements Connectable, ConnectionListener
{
    private final Logger log = Logger.getLogger(getClass());
    private final ServerProxy serverProxy;
    private boolean connected;
    private boolean reconnect = true;

    public ReconnectLayer(ServerProxy serverProxy)
    {
        this.serverProxy = serverProxy;
        serverProxy.addListener(this);
    }

    public boolean connect()
    {
        connectWithRetry();
        return false;
    }

    public void disconnected()
    {
        connected = false;
        if (reconnect)
        {
            connectWithRetry();
        }
    }

    @Override
    public void connected()
    {
    }

    private void connectWithRetry()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                while (!connected)
                {
                    connected = serverProxy.connect();
                    if (!connected)
                    {
                        ThreadUtils.sleep(3000);
                    }
                    else
                    {
                        connected = true;
                    }
                }
            }
        }).start();

    }

    @Override
    public void disconnect()
    {
        serverProxy.disconnect();
    }

    @Override
    public void handleConnectException(Exception e)
    {

    }

    @Override
    public boolean isConnected()
    {
        return serverProxy.isConnected();
    }

    @Override
    public String toString()
    {
        return serverProxy.toString();
    }

    public void setReconnect(boolean reconnect)
    {
        this.reconnect = reconnect;
    }
}