/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;


import net.sf.xapp.uifwk.XPane;

public class Resizer implements Task
{
    private XPane comp;
    private float startScaleX;
    private float startScaleY;
    private float endScaleX;
    private float endScaleY;
    private float initialW;
    private float initialH;
    private int anchorX;
    private int anchorY;

    public Resizer(XPane comp, float startScaleX, float startScaleY, float endScaleX, float endScaleY,
                   int anchorX, int anchorY)
    {
        this.comp = comp;
        this.startScaleX = startScaleX;
        this.startScaleY = startScaleY;
        this.endScaleX = endScaleX;
        this.endScaleY = endScaleY;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    @Override
    public void init()
    {
        initialW = comp.getWidth();
        initialH = comp.getHeight();
    }

    @Override
    public void step(double workDone, Object... args)
    {
        float newScaleX = Helper.linearInterpolate(startScaleX, endScaleX, workDone);
        float newScaleY = Helper.linearInterpolate(startScaleY, endScaleY, workDone);
        int ow = comp.getOriginalWidth();
        int oh = comp.getOriginalHeight();
        int w = (int) (ow * newScaleX);
        int h = (int) (oh * newScaleY);
        float xOffSet = (ow - w) / 2;
        float yOffSet = (oh - h) / 2;
        int x = (int) (anchorX + xOffSet);
        int y = (int) (anchorY + yOffSet);
        comp.setLocation(x,y);
        comp.setSize(w,h);
    }
}
