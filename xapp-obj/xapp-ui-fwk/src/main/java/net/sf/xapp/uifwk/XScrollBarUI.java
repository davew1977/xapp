/*
 *
 * Date: 2011-feb-03
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import java.awt.*;

public class XScrollBarUI extends MetalScrollBarUI
{

    @Override
    protected void paintThumb(final Graphics gr, final JComponent c, final Rectangle thumbBounds)
    {
        Graphics2D g = (Graphics2D) gr;
        g.setPaint(new GradientPaint(thumbBounds.x, thumbBounds.y, Color.darkGray, thumbBounds.x + thumbBounds.width, thumbBounds.y +10, Color.lightGray));
        g.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
        g.setColor(Color.gray);
        g.drawRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width-1, thumbBounds.height-1, 10, 10);
    }

    @Override
    protected void paintIncreaseHighlight(Graphics g)
    {

    }

    @Override
    protected JButton createDecreaseButton(int orientation)
    {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0,0));
        return b;
    }

    @Override
    protected JButton createIncreaseButton(int orientation)
    {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0,0));
        return b;
    }

    @Override
    protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds)
    {
        /*g.setColor(Color.black);
        g.fillRect(trackBounds.width / 2, trackBounds.y, 3, trackBounds.height);
        if (this.trackHighlight == BasicScrollBarUI.DECREASE_HIGHLIGHT)
        {
            this.paintDecreaseHighlight(g);
        }
        else if (this.trackHighlight == BasicScrollBarUI.INCREASE_HIGHLIGHT)
        {
            this.paintIncreaseHighlight(g);
        }*/

        //g.fillRect(trackBounds.x,trackBounds.y,trackBounds.width,trackBounds.height);
    }
}
