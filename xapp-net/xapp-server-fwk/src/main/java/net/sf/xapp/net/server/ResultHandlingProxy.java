/*
 *
 * Date: 2010-sep-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server;

import net.sf.xapp.net.api.out.Out;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables sync and async api implentations to handle errors in a consistent manner
 * @param <A>
 */
public class ResultHandlingProxy<A> implements MessageHandler<A>
{
    private final Logger log = LoggerFactory.getLogger(getClass());
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
