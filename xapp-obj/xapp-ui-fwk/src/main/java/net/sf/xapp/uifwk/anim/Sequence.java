/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

import java.util.ArrayList;
import java.util.List;

public class Sequence
{

    List<TaskTuple> tasks;
    private int index;
    boolean loop;

    public Sequence()
    {
        tasks = new ArrayList<TaskTuple>();
        index = -1;
        loop = false;
    }

    public void start(long time)
    {
        index = -1;
        nextTask(time);
    }

    public void stop()
    {
        index = -1;
    }

    public void end()
    {
        if (index != -1)
        {
            for (int i = index; i < tasks.size(); i++)
            {
                TaskTuple tt = tasks.get(i);
                tt.task.init();
                tt.task.step(1, tt.attachment);
            }
            index = -1;
        }
    }


    public boolean tick(long now)
    {
        if (index == -1)
        {
            return false;
        }
        TaskTuple tt = currentTask();
        long timeSinceStart = now - tt.startTime;
        if (now >= tt.startTime + tt.duration || tt.duration==0) //task done
        {
            tt.task.step(1, tt.attachment);
            nextTask(now);
        }
        else
        {
            double workDone = timeSinceStart / (float) tt.duration;
            workDone = tt.effect.transform(workDone);
            tt.task.step(workDone, tt.attachment);
        }
        return true;
    }


    private TaskTuple currentTask()
    {
        return tasks.get(index);
    }


    private void nextTask(long time)
    {
        index++;
        if (index < tasks.size())
        {
            currentTask().startTime = time;
            currentTask().task.init();
            tick(currentTask().startTime);
        }
        else
        {
            if (loop)
            {
                index = -1;
                nextTask(time);
            }
            else
            {
                index = -1;
//					end();
            }
        }
    }
}
