/*
 *
 * Date: 2011-jan-21
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduledTask implements Runnable
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public final void run()
    {
        try
        {
            execute();
        }
        catch(Throwable t)
        {
            log.error("exception caught in scheduled task", t);

        }
    }

    public abstract void execute();
}
