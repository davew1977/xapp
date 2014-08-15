/*
 *
 * Date: 2011-feb-07
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.framework;

public class Logger
{
    private static Logger instance = new Logger();
    private static boolean debugEnabled = Boolean.getBoolean("debug.enabled");



    public static Logger getLogger(Class<?> aClass)
    {
        return instance;
    }

    public void info(Object s)
    {
        System.out.println(s);
    }

    public void debug(Object s)
    {
        if (debugEnabled)
        {
            System.out.println(s);
        }
    }
}
