/*
 *
 * Date: 2011-feb-01
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;










import java.awt.*;

public class GUIHelper
{

    public static XButton button(Object listener, String listenerMethod)
    {
        return button(listener, listenerMethod, listenerMethod);
    }
    public static XButton button(Object listener, String label, String listenerMethod)
    {
        XButton b = new XButton(label);
        b.addListener(listener, listenerMethod);
        b.size(100,20);
        return b;
    }

    public static XPane createVerticalButtonPane(XButton... buttons)
    {
        XPane pane = new XPane();
        int w=0,h=0;
        for (int i = 0; i < buttons.length; i++)
        {
            XButton button = buttons[i];
            button.location(0, i* 20);
            pane.add(button);
            w = Math.max(w, button.getWidth());
            h+=button.getHeight();
        }
        pane.setSize(w,h);
        return pane;
    }

    public static Color applyAlpha(Color c, int alpha)
    {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static void paintBot(Graphics2D g)
    {

    }

    public static void addRolloverGlow(XPane pane, int thickness, int arcWidth)
    {
        RoundRectGlow glow = new RoundRectGlow();
        glow.init(arcWidth, pane.getWidth()- thickness * 2 , pane.getHeight() - thickness*2, thickness);
        pane.add(glow);
        pane.setBorderWidth(thickness);
        glow.setTwinkleCount(0);
        pane.addOnMouseEnter(new Callback("glow", glow, true));
        pane.addOnMouseExit(new Callback("glow", glow, false));

    }
}
