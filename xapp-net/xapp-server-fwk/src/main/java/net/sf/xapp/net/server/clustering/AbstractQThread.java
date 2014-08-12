/*
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;

/**
 * Provides functionality common to the {@link NodeQThread} and the
 * {@link ClusterQThread}
 */
public abstract class AbstractQThread extends Thread implements QHandler
{
    protected final ClusterFacade cluster;
    private final boolean startThread;
    private final Logger log = Logger.getLogger(getClass());
    private boolean m_alive = true;

    public AbstractQThread(ClusterFacade cluster, boolean startThread)
    {
        this.cluster = cluster;
        this.startThread = startThread;
    }

    public final void run()
    {
        String className = getClass().getSimpleName();
        log.info(String.format("started %s: %s", className,this));
        /*
         *  Hard shutting down the node will actually kill the node q handler thread.
         */
        while (m_alive)
        {
            try
            {
                /*
                 * poll will continue to block for up to one second even after the node is in SHUTDOWN state
                 */
                InMessage inMessage = getQueueToPoll().take();
                processMessage(inMessage);
            }
            catch (InterruptedException e)
            {
                log.info(className + " was interrupted");
            }
            catch (Exception e)
            {
                log.error(String.format("Exception caught in %s", this),e);
            }
        }
        log.info(String.format("stopping %s Thread: %s", className, this));
    }

    abstract BlockingQueue<InMessage> getQueueToPoll();

    public final void init()
    {
        if (startThread)
        {
            start();
        }
        else
        {
            log.info(String.format("%s NOT started", getClass().getSimpleName()));
        }
        log.info("Initialized " + getClass().getSimpleName());
    }

    @PreDestroy
    public final void shutdown()
    {
        m_alive = false;
        interrupt();
    }
}