/*
 *
 * Date: 2010-apr-30
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.eventloop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Manages all the event loops, mapped to a key.
 */
public class EventLoopManager
{
    private final Map<String, EventLoop> m_eventLoops;
    private final Executor m_executor;

    public EventLoopManager(Executor executor)
    {
        m_executor = executor;
        m_eventLoops = new ConcurrentHashMap<String, EventLoop>();
    }

    /**
     *
     * @param key
     * @param task
     * @param removeOnExit true if the resources can be removed for this key when the task is complete
     *                      The loop will only be removed if nothing more has been posted to it when this
     *                      task completes
     */
    public void postTask(final String key, Runnable task, final boolean removeOnExit)
    {
        final EventLoop loop = getLoop(key);
        loop.addTask(new LoopTask(task)
        {
            @Override
            public void taskComplete()
            {
                if (removeOnExit)
                {
                    synchronized (EventLoopManager.this)
                    {
                        if (loop.isEmpty())
                        {
                            m_eventLoops.remove(key);
                        }
                    }
                }
            }
        });
    }

    private synchronized EventLoop getLoop(String key)
    {
        EventLoop tempLoopRef;
        tempLoopRef = m_eventLoops.get(key);
        if (tempLoopRef == null)
        {
            tempLoopRef = new EventLoop(m_executor);
            m_eventLoops.put(key, tempLoopRef);
        }
        return tempLoopRef;
    }

    public int countEventLoops()
    {
        return m_eventLoops.size();
    }

    /**
     * posts a task on the event loop to remove itself. However, there is not guarantee from the EventLoopManager that a
     * "post" won't be performed afterwards, which would create a new event loop for the key.
     * @param key
     */
    public void remove(String key)
    {
        postTask(key, new DoNothingTask(), true);
    }

    private class DoNothingTask implements Runnable
    {
        @Override
        public void run()
        {

        }
    }
}