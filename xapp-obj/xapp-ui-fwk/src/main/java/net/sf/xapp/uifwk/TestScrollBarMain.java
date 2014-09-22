/*
 *
 * Date: 2010-dec-15
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import java.awt.*;

public class TestScrollBarMain extends JFrame
{

    public TestScrollBarMain()
    {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(500, 500));
        JScrollPane s = new JScrollPane(p);
        MyScrollBar b = new MyScrollBar();
        s.setVerticalScrollBar(b);
        getContentPane().add(s);
        setSize(100, 100);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        new TestScrollBarMain();
    }

    public class MyScrollBarUI extends MetalScrollBarUI
    {

        @Override
        protected void paintThumb(final Graphics g, final JComponent c, final Rectangle thumbBounds)
        {
            g.setColor(Color.white);
            g.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 5, 5);
        }

        @Override
        protected void paintIncreaseHighlight(Graphics g)
        {
            
        }

        @Override
        protected JButton createDecreaseButton(int orientation)
        {
            return new XButton("");
        }

        @Override
        protected JButton createIncreaseButton(int orientation)
        {
            return new XButton("");
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
        }
    }

    public class MyScrollBar extends JScrollBar
    {

        MyScrollBar()
        {
            super();
            setUI(new MyScrollBarUI());
        }
    }

}


