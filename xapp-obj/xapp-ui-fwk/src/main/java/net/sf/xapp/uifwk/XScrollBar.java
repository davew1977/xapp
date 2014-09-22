/*
 *
 * Date: 2011-feb-03
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import java.awt.*;

public class XScrollBar extends JScrollBar
{
    public XScrollBar()
    {
        this(JScrollBar.VERTICAL);
    }
    public XScrollBar(int orientation)
    {
        super(orientation);
        setUI(new XScrollBarUI());
        setOpaque(false);
    }

    public XScrollBar(int orientation, int value, int extent, int min, int max)
    {
        super(orientation, value, extent, min, max);
        setUI(new XScrollBarUI());
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        super.paintComponent(g);
    }
}
