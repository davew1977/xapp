/*
 *
 * Date: 2010-okt-28
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.framework;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;

import javax.swing.*;

/**
 * can dispa
 * @param <A>
 */
public class EvtDispatcherMessageHandler<A> implements MessageHandler<A>
{
    private final A delegate;
    private final MessageHandler<A> messageHandler;

    public EvtDispatcherMessageHandler(A delegate)
    {
        this.delegate = delegate;
        this.messageHandler = null;
    }

    public EvtDispatcherMessageHandler(MessageHandler<A> messageHandler)
    {
        this.delegate = null;
        this.messageHandler = messageHandler;
    }

    @Override
    public <T> T handleMessage(final InMessage<A, T> atInMessage)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (delegate!=null)
                {
                    atInMessage.visit(delegate);
                }
                else
                {
                    messageHandler.handleMessage(atInMessage);
                }
            }
        });
        return null;
    }
}
