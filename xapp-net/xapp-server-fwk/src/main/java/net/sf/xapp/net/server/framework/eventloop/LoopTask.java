/*
 *
 * Date: 2010-apr-30
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.eventloop;


public abstract class LoopTask
{
    private final Runnable m_userTask;

    public LoopTask(Runnable userTask)
    {
        m_userTask = userTask;
    }

    abstract void taskComplete();

    public final void run()
    {
        m_userTask.run();
    }
}