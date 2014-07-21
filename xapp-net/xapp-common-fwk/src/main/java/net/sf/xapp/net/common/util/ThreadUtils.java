/*
 *
 * Date: 2010-sep-14
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;

public class ThreadUtils
{
    public static void sleep(int timeInMillis)
    {
        try
        {
            Thread.sleep(timeInMillis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
