/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

public interface Task
{

    /*
          * necessary for tasks that will be run later
          */
    void init();

    /*
          *	is called throughout the tasks execution with an increasing
          *  value between 0 & 1.0. A task that is scheduled without duration will have
          *  this method invoked once with a value of 1.0
          *
          *  The optional attachment was provided when the "stage director" posted the task
          */
    void step(double workDone, Object... args);
}
