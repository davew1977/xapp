/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

import java.awt.*;

public class Mover implements Task
{
    private Component comp;
    private Point start;
    private Point end;
    private boolean relative; //relative means we want to move the component from where it is when the animation starts

    public Mover(Component comp, Point start, Point end)
    {
        this.comp = comp;
        this.start = start;
        relative = start == null;
        this.end = end;
    }

    public void init()
    {
        if (relative) start = new Point(comp.getLocation());
    }

    public void step(double workDone, Object... args)
    {
        int x = Math.round(Helper.linearInterpolate(start.x, end.x, workDone));
        int y = Math.round(Helper.linearInterpolate(start.y, end.y, workDone));
        comp.setLocation(x,y);
    }
}
