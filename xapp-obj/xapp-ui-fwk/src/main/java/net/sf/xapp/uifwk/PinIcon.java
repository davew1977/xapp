package net.sf.xapp.uifwk;



import java.awt.*;

public class PinIcon extends XPane
{
    public PinIcon()
    {
        setDefaultSize(15,15);

    }



    @Override
    protected void paintPane(ScalableGraphics g)
    {
        super.paintPane(g);    //To change body of overridden methods use File | Settings | File Templates.
        g.gradientPaint(3, 12, Color.white, 10, 5, Color.gray);
        g.setPenThickness(2);
        g.drawLine(3,12,10,5);
        g.radialPaint(11, 4, 6, Color.white, Color.red);
        g.fillOval(6,1,8,8);

    }

    public static void main(String[] args)
    {
        PinIcon p = new PinIcon();
        p.setSize(80,80);
        SwingUtils.showInFrame(p);
    }
}
