/*
 *
 * Date: 2010-mar-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.io.*;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.TransportHelper;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.net.common.util.StringUtils;

/**
 * Wraps a server proxy and a {@link ScriptPreprocessor}
 */
public class ScriptAdminClient implements Processor, ConnectionListener, MessageHandler
{
    private HostInfo hostInfo;
    private MessageHandler server;
    private Connectable connectable;
    private ScriptPreprocessor scriptPreprocessor;
    private MultiNodeClientGUI<ScriptAdminClient> gui;

    public ScriptAdminClient(HostInfo hostInfo, TestData testData)
    {
        this.hostInfo = hostInfo;
        scriptPreprocessor = new ScriptPreprocessor(testData);
        scriptPreprocessor.setProcessor(this);
    }

    public void init(ServerProxy serverProxy, MultiNodeClientGUI<ScriptAdminClient> gui)
    {
        this.gui = gui;
        this.server = serverProxy;
        this.connectable = new ReconnectLayer(serverProxy);
        serverProxy.addListener(this);
        connectable.connect(true);
        if (this.gui !=null)
        {
            this.gui = gui;
            this.gui.print(this, String.format("connecting to %s", connectable));
        }
    }

    @Override
    public void connectionStateChanged(ConnectionState newState) {

        switch (newState) {

            case ONLINE:
                gui.println(this, String.format("\nconnected to %s", connectable));
                break;
            case OFFLINE:
                gui.println(this, String.format("disconnected from %s", connectable));
                break;
            case CONNECTING:
                gui.print(this, "\nconnecting...");
                break;
            case CONNECTION_LOST:
                break;
        }
    }

    @Override
    public void exec(String message)
    {
        if (message.equals("connect"))
        {
            connectable.connect(true);
        }
        else if (message.equals("disconnect"))
        {
            connectable.setOffline();
        }
        else
        {
            server.handleMessage(TransportHelper.<InMessage>fromString(message));
        }
    }

    @Override
    public Object handleMessage(InMessage inMessage)
    {
        gui.println(this, StringUtils.unescapeSpecialChars(inMessage.toString()));
        return null;
    }

    public void sendMessage(String command)
    {
        scriptPreprocessor.exec(command);
    }

    public void runScript(Script script)
    {
        scriptPreprocessor.runScript(script.getContent());
    }

    public MessageHandler getServer()
    {
        return server;
    }

    @Override
    public void handleConnectException(Exception e)
    {

    }

    @Override
    public String toString()
    {
        return hostInfo.toString() ;
    }
}