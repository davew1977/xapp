/*
 *
 * Date: 2010-nov-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageInvocation<A> implements Runnable
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final A api;
    private final InMessage<A,Void> message;

    public MessageInvocation(A api, InMessage<A, Void> message)
    {
        this.api = api;
        this.message = message;
    }

    @Override
    public void run()
    {
        try
        {
            message.visit(api);
        }
        catch (Throwable e)
        {
            log.error(String.format("exception caught handling message %s, api is %s", message, api), e);
        }
    }
}
