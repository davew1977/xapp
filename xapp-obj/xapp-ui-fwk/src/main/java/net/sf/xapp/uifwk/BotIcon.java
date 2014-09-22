package net.sf.xapp.uifwk;



import java.awt.*;

public class BotIcon extends XPane
{

    public Color color = Color.magenta;

    public BotIcon()
    {
        setDefaultSize(20,20);

    }

    @Override
    protected void paintPane(ScalableGraphics g)
    {
        g.setColor(Color.white);
        g.fillArc(2,0,12,12,0,180);
        g.fillRect(2,6,12,10);
        g.fillRoundRect(2,14,12,4,4,4);
        g.gradientPaint(0,0,Color.darkGray, 20,20,color);
        g.fillArc(3,1,10,10,0,180);
        g.fillRect(3,7,10,4);
        g.fillRoundRect(3,10,10,3,3,3);
        g.fillRect(4.5f,14,3,2);
        g.fillRoundRect(4.5f,14,3,3,2,2);
        g.fillRect(9,14,3,2);
        g.fillRoundRect(9,14,3,3,2,2);

        g.gradientPaint(5,4,Color.white,7,6,Color.gray);
        g.fillOval(4.5f,4,1.2f,1.2f);
        g.gradientPaint(9.8f, 4, Color.white, 11.8f, 6, Color.gray);
        g.fillOval(9.3f,4,1.2f,1.2f);
    }

    public static void main(String[] args)
    {
        BotIcon p = new BotIcon();
        p.setSize(100,100);
        XPane p2 = new XPane();
        p2.setSize(100,100);
        p2.add(p);
        SwingUtils.showInFrame(p2);
    }
}
