/*
 *
 * Date: 2010-dec-15
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import java.awt.*;

public class XScrollPane extends JScrollPane
{
    public XScrollPane(Component view)
    {
        this(view, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    public XScrollPane(Component view, int horizontalPolicy)
    {
        super(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, horizontalPolicy);
        setOpaque(false);
        getViewport().setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setVerticalScrollBar(new XScrollBar());
        getVerticalScrollBar().setPreferredSize(new Dimension(12,
                getVerticalScrollBar().getPreferredSize().height));
        setHorizontalScrollBar(new XScrollBar(JScrollBar.HORIZONTAL));
        getHorizontalScrollBar().setPreferredSize(new Dimension(
               getHorizontalScrollBar().getPreferredSize().width, 12));

    }
}
