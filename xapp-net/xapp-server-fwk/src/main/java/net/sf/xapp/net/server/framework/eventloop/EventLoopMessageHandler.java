/*
 *
 * Date: 2010-jun-23
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.eventloop;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.server.framework.Decorator;
import org.apache.log4j.Logger;

public class EventLoopMessageHandler<A> implements Decorator<A>
{
    private final Logger log = Logger.getLogger(getClass());
    private final EventLoopManager eventLoopManager;
    private final A delegate;
    private final String keyOverride;

    public EventLoopMessageHandler(EventLoopManager eventLoopManager, A delegate)
    {
        this(eventLoopManager, delegate, null);
    }

    /**
     *
     * @param eventLoopManager
     * @param delegate
     * @param keyOverride if the key is known before hand, but not at runtime, then use this
     */
    public EventLoopMessageHandler(EventLoopManager eventLoopManager, A delegate, String keyOverride)
    {
        this.eventLoopManager = eventLoopManager;
        this.delegate = delegate;
        this.keyOverride = keyOverride;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        String entityKey = keyOverride!=null ? keyOverride : inMessage.entityKey();
        eventLoopManager.postTask(entityKey, new Task<T>(inMessage), false);
        return null;
    }

    public A getDelegate()
    {
        return delegate;
    }

    private class Task<T> implements Runnable
    {
        private final InMessage<A, T> message;

        public Task(InMessage<A, T> message)
        {
            this.message = message;
        }

        @Override
        public void run()
        {
            //log.debug("processing " + message);
            message.visit(delegate);
        }
    }
}
