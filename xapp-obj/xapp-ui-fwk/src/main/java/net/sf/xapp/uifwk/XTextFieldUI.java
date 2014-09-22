/*
 *
 * Date: 2011-feb-15
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;

public class XTextFieldUI extends BasicTextFieldUI
{
    private JTextField tf;

    public XTextFieldUI(JTextField tf)
    {
        this.tf = tf;
    }

    @Override
    public void update(Graphics g, JComponent c)
    {
        paintBgrd(g, tf);
        super.update(g, c);
    }

    public void paintBgrd(Graphics g, JTextField tf)
    {
        _paintBgrd(g, tf);
    }

    public static void _paintBgrd(Graphics g, JTextField tf)
    {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(tf.getBackground());
        g.fillRoundRect(0, 0, tf.getWidth() - 1, tf.getHeight() - 1, 10, 10);
        g.setColor(Color.gray);
        ((Graphics2D) g).setStroke(new BasicStroke(1));
        g.drawRoundRect(0, 0, tf.getWidth()-1, tf.getHeight()-1, 10 , 10);
    }

}
