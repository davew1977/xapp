/*
 *
 * Date: 2010-sep-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver.mina;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.Message;
import net.sf.xapp.net.server.connectionserver.IOLayer;
import net.sf.xapp.net.server.connectionserver.MessageLayer;
import org.apache.log4j.Logger;
import org.apache.mina.common.*;

public class MinaIOHandler<T> implements IoHandler, IOLayer<IoSession, T>
{
    private final Logger log = Logger.getLogger(getClass());
    private final MessageLayer<IoSession, T> messageLayer;
    private final String SESSION_KEY = "sessionKey";


    public MinaIOHandler(MessageLayer<IoSession, T> messageLayer)
    {
        this.messageLayer = messageLayer;
        messageLayer.setIOLayer(this);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception
    {

    }

    @Override
    public void sessionOpened(IoSession session) throws Exception
    {
        messageLayer.sessionOpened(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception
    {
        messageLayer.sessionClosed(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception
    {
         //TODO
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception
    {

    }

    @Override
    public void messageReceived(IoSession session, Object obj) throws Exception
    {
        try
        {
            InMessage m = (InMessage) obj;
            messageLayer.handleMessage(session, m);
        }
        catch (Throwable e)
        {
            log.error("error handling message: " + obj, e);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception
    {

    }

    @Override
    public void sendMessage(IoSession connection, Message message)
    {
        connection.write(message);
    }

    @Override
    public void closeSession(IoSession connection)
    {
        connection.close();
    }

    @Override
    public T getSessionKey(IoSession session)
    {
        return (T) session.getAttribute(SESSION_KEY);
    }

    @Override
    public void setSessionKey(IoSession session, T sessionKey)
    {
        session.setAttribute(SESSION_KEY, sessionKey);
    }

    @Override
    public String toString()
    {
        return "MinaIOHandler; message layer is " + messageLayer.getClass().getSimpleName();
    }
}
