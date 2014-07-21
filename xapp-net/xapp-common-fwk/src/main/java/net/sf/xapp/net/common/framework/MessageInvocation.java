/*
 *
 * Date: 2010-nov-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import net.sf.xapp.net.common.framework.InMessage;
import org.apache.log4j.Logger;

public class MessageInvocation<A> implements Runnable
{
    private final Logger log = Logger.getLogger(getClass());
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
