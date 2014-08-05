/*
 *
 * Date: 2010-mar-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.framework.TransportHelper;
import ngpoker.common.util.StringUtils;

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
        connectable.connect();
        if (this.gui !=null)
        {
            this.gui = gui;
            this.gui.print(this, String.format("connecting to %s", connectable));
        }
    }

    @Override
    public void connected()
    {
        gui.println(this, String.format("\nconnected to %s", connectable));
    }

    public void disconnected()
    {
        gui.println(this, String.format("disconnected from %s", connectable));
        gui.print(this, "\nreconnecting...");
    }

    @Override
    public void exec(String message)
    {
        if (message.equals("connect"))
        {
            connectable.connect();
        }
        else if (message.equals("disconnect"))
        {
            connectable.disconnect();
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