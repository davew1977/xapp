/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import ngpoker.appserver.Out;
import ngpoker.appserver.OutAdaptor;
import net.sf.xapp.net.server.clustering.PublicEntryPoint;
import net.sf.xapp.net.common.framework.InMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminMessageLayer<T> extends OutAdaptor implements MessageLayer<T, Integer>
{
    private final PublicEntryPoint publicEntryPoint;
    private final AtomicInteger sessionSeq;
    private IOLayer<T, Integer> ioLayer;
    private Map<Integer, T> sessions;

    public AdminMessageLayer(PublicEntryPoint publicEntryPoint)
    {
        super(null);
        this.publicEntryPoint = publicEntryPoint;
        sessions = new HashMap<Integer,T>();
        sessionSeq = new AtomicInteger(0);
    }

    @Override
    public void setIOLayer(IOLayer<T, Integer> ioLayer)
    {
        this.ioLayer = ioLayer;
    }

    @Override
    public void sessionOpened(T session)
    {
        int sessionKey = sessionSeq.getAndIncrement();
        sessions.put(sessionKey, session);
        ioLayer.setSessionKey(session, sessionKey);
    }

    @Override
    public void sessionClosed(T session)
    {
        int sessionKey = ioLayer.getSessionKey(session);
        sessions.remove(sessionKey);
    }

    @Override
    public void handleMessage(T session, InMessage message)
    {
        publicEntryPoint.handleMessage(message);
    }

    @Override
    public <E> E handleMessage(InMessage<Out, E> inMessage)
    {
        for (T t : sessions.values())
        {
            ioLayer.sendMessage(t, inMessage);
        }
        return null;
    }
}