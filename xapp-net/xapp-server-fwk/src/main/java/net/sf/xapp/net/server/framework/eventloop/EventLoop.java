/*
 *
 * Date: 2010-apr-30
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.eventloop;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Encapsulates a task queue and holds a reference to a thread pool.
 *
 * tasks added to the "loop" are guaranteed to execute in the order submitted, but the thread pool will
 * be utilised to do the work.
 *
 * One way to get simple application logic is to assume a single threaded environment. For a given context, say, a
 * tournament, it is nice to assume that all operations will be called one at a time. A naive implementation of this could be
 * to allocate one thread per tournament. A better design, however, is to decouple the application's domain context
 * object (the tournament) from the threading allocation. This is what the event loop does. One instance will exist per
 * tournament.
 */
public class EventLoop
{
    private final Queue<LoopTask> m_taskQueue;
    private final Executor m_executor;

    public EventLoop(Executor executor)
    {
        m_executor = executor;
        m_taskQueue = new LinkedList<LoopTask>();
    }

    public synchronized void addTask(LoopTask task)
    {
        /*
        if there are no pending tasks, then execute this one immediately.
        Otherwise, add to the pending task queue
         */
        if(m_taskQueue.isEmpty())
        {
            postTask(task);
        }
        m_taskQueue.add(task);
    }

    private void postTask(final LoopTask task)
    {
        m_executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                /*
                execute the task.
                Finally, check to see if there is another pending task and if there is, execute it
                 */
                try
                {
                    task.run();
                }
                finally
                {
                    synchronized (EventLoop.this)
                    {
                        m_taskQueue.remove();
                        if(!m_taskQueue.isEmpty())
                        {
                            postTask(m_taskQueue.peek());
                        }
                    }
                    task.taskComplete();
                }
            }
        });
    }

    public boolean isEmpty()
    {
        return m_taskQueue.isEmpty();
    }

    public Queue<LoopTask> getTaskQueue()
    {
        return m_taskQueue;
    }
}