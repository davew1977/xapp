/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;

import java.util.concurrent.Executor;

public class ThreadPoolInvoker<A> implements MessageHandler<A>
{
    private final A delegate;
    private final Executor executor;

    public ThreadPoolInvoker(A delegate, Executor executor)
    {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public <T> T handleMessage(final InMessage<A, T> inMessage)
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                inMessage.visit(delegate);
            }
        });
        return null;
    }
}
