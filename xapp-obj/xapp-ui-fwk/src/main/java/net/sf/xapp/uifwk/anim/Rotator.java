/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import net.sf.xapp.uifwk.XPane;

public class Rotator implements Task
{
    private XPane comp;
    private float startAngle;
    private float endAngle;

    public Rotator(XPane comp, float startAngle, float endAngle)
    {
        this.comp = comp;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    @Override
    public void init()
    {
    }

    @Override
    public void step(double workDone, Object... args)
    {
        float newRotation = Helper.linearInterpolate(startAngle, endAngle, workDone);
        comp.setRotation(newRotation);
    }
}
