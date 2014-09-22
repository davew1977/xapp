/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import net.sf.xapp.uifwk.XPane;

public class Fader implements Task
{
    private XPane comp;
    private float m_startAlpha;
    private float m_endAlpha;
    private boolean m_relative;

    public Fader(XPane comp, float start, float end)
    {
        this.comp = comp;
        m_startAlpha = start;
        m_relative = start == -1;
        m_endAlpha = end;
    }

    public void init()
    {
        if (m_relative) m_startAlpha = comp.getDefaultAlpha();
    }

    public void step(double workDone, Object... args)
    {
        comp.setDefaultAlpha((float) Helper.linearInterpolate(m_startAlpha, m_endAlpha, workDone));
        comp.repaint();
    }
}
