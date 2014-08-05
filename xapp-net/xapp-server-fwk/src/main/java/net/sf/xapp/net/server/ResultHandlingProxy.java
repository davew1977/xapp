/*
 *
 * Date: 2010-sep-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server;

import ngpoker.appserver.Out;
import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.GenericException;
import ngpoker.common.types.MessageTypeEnum;
import org.apache.log4j.Logger;

/**
 * Enables sync and async api implentations to handle errors in a consistent manner
 * @param <A>
 * @param <B>
 */
public class ResultHandlingProxy<A> implements MessageHandler<A>
{
    private final Logger log = Logger.getLogger(getClass());
    private final Out out;
    private final A delegate;

    public ResultHandlingProxy(Out out, A delegate)
    {
        this.out = out;
        this.delegate = delegate;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> m)
    {
        T result = null;
        try
        {
            result = m.visit(delegate);
            out.success(m.type());
        }
        catch (GenericException e)
        {
            log.info("exception: ", e);
            out.failure(m.type(), e.getErrorCode(), e.getMessage());
        }
        return result;
    }
}
