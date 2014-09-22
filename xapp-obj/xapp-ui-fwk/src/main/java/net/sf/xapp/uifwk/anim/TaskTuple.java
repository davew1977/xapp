/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

public class TaskTuple
{
    int duration;
    Task task;
    Object[] attachment;
    Effect effect;
    long startTime;

    public TaskTuple(Task task, int duration, Effect effect, Object... attachment)
    {
        this.duration = duration;
        this.task = task;
        this.effect = effect;
        this.attachment = attachment;
    }

}
