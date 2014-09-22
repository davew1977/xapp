/*
 *
 * Date: 2011-mar-02
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;



import java.awt.*;

public class XGradientPane extends XPane
{
    private Color[] background;
    protected int leftInset = 20;

    public XGradientPane()
    {
        setDefaultAlpha(0.8f);
        background = new Color[2];
        background(Color.darkGray, Color.lightGray);
    }

    public XGradientPane background(Color c1, Color c2)
    {
        background[0] = c1;
        background[1] = c2;
        return this;
    }

    public XGradientPane background(Color c)
    {
        return background(c,c);
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        if (background[0]!=null)
        {
            g.setPaint(new GradientPaint(0,0,background[0],0,10,background[1]));
            g.fillRoundRect(0,0,getWidth(), getHeight(), leftInset*2, leftInset*2);
        }
    }

    public static void main(String[] args)
    {
        XGradientPane i = new XGradientPane().background(Color.blue, Color.YELLOW);
        i.setSize(200,200);
        SwingUtils.showInFrame(i);
    }
}
