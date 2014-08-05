/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import org.apache.log4j.Logger;

public class LoggingMessageHandler<A> implements MessageHandler<A>
{
    private Logger log = Logger.getLogger(getClass());

    @Override
    public <T> T handleMessage(InMessage<A, T> message)
    {
        log.info(message);
        return null;
    }
}
