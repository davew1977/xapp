/*
 *
 * Date: 2009-nov-16
 * Author: davidw
 *
 */
package net.sf.xapp.application.api;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class SimpleTreeGraphics implements SpecialTreeGraphics
{
    public static ImageIcon loadImage(String url)
    {
        return new ImageIcon(SimpleTreeGraphics.class.getResource(url), "");
    }

    public void init(ApplicationContainer applicationContainer)
    {

    }

    public ImageIcon getNodeImage(Node node)
    {
        return null;
    }

    public void decorateCell(Node node, Graphics2D g)
    {

    }

    public String getTooltip(Node node)
    {
        return null;
    }

    public void prepareRenderer(Node currentNode, DefaultTreeCellRenderer cellRenderer)
    {

    }
}
