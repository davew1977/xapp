/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMessageHandler<A> implements MessageHandler<A>
{
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public <T> T handleMessage(InMessage<A, T> message)
    {
        log.info(message.toString());
        return null;
    }
}
