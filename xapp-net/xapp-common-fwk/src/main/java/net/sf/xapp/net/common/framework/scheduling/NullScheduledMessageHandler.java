package net.sf.xapp.net.common.framework.scheduling;

import net.sf.xapp.net.common.framework.InMessage;

public class NullScheduledMessageHandler<A> implements ScheduledMessageHandler<A>
{
    @Override
    public void init(A api)
    {

    }

    @Override
    public Task invokeLater(InMessage<A, Void> aVoidInMessage, long delay)
    {
        return null;
    }
}
