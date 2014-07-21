/*
 *
 * Date: 2010-nov-03
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.scheduling;

public interface Task
{
    /**
     *
     * @return  true if cancel was successful
     */
    boolean cancel();
}
