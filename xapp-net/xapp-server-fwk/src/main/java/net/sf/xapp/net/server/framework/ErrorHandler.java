package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

public class ErrorHandler<A> implements MessageHandler<A>
{
    private final A delegate;
    private final ExceptionListener exceptionListener;

    public ErrorHandler(A delegate, ExceptionListener exceptionListener)
    {
        this.delegate = delegate;
        this.exceptionListener = exceptionListener;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> message)
    {
        try
        {
            return message.visit(delegate);
        }
        catch(Throwable e)
        {
            exceptionListener.exceptionThrown(e);
        }
        return null;
    }
}
