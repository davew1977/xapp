/*
 *
 * Date: 2010-dec-13
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import java.awt.*;

public class XTable extends JTable
{
    public XTable()
    {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

        super.paintComponent(g);
    }
}
