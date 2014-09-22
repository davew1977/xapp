/*
 *
 * Date: 2010-dec-16
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class XTabbedPane extends JTabbedPane
{
    public XTabbedPane()
    {
        setUI(new MyBasicTabbedPaneUI());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        getInputMap().getParent().clear();
    }

    public void addCloseableTab(String title, Component component, ActionListener listener)
    {
        addTab(title, component);
        setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this, listener));
    }

    private class MyBasicTabbedPaneUI extends BasicTabbedPaneUI
    {
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex)
        {
            if(getTabCount()>0)
            {
                super.paintContentBorder(g, tabPlacement, selectedIndex);
            }
        }

        @Override
        protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
        {
            g.setColor(Color.black);
            g.fillRect(x, y + 4, 2, h);
            if(selectedIndex!=0)
            {
                g.fillRect(0,y,2,4);
            }
        }

        @Override
        protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
        {
            g.setColor(Color.black);
            g.fillRect(x, y + h - 3, w, 3);
        }

        @Override
        protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h)
        {
            g.setColor(Color.black);
            g.fillRect(x + w -3, y, 3, h);
        }

        @Override
        protected void paintTabBackground(Graphics gra, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
        {
            Graphics2D g = (Graphics2D) gra;
            GradientPaint gp = new GradientPaint(0,0, Color.gray, 0,10, isSelected ? Color.white : Color.lightGray);
            g.setPaint(gp);
            g.fillRect(x+1, y+1, w-3, h-1);
        }
    }
}
