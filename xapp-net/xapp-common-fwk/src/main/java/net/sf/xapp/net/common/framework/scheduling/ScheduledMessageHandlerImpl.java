/*
 *
 * Date: 2010-nov-03
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.scheduling;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageInvocation;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledMessageHandlerImpl<A> implements ScheduledMessageHandler<A>
{
    protected final ScheduledExecutorService executorService;
    protected A api;

    public ScheduledMessageHandlerImpl(ScheduledExecutorService executorService, A api)
    {
        this.executorService = executorService;
        this.api = api;
    }

    @Override
    public void init(A api)
    {
        this.api = api;
    }

    @Override
    public Task invokeLater(final InMessage<A, Void> message, long delay)
    {
        assert api!=null;
        final ScheduledFuture<?> scheduledFuture = executorService.schedule(
                new MessageInvocation<A>(api, message),
                delay, TimeUnit.MILLISECONDS);

        return new Task()
        {
            @Override
            public boolean cancel()
            {
                return scheduledFuture.cancel(false);
            }
        };
    }

}
