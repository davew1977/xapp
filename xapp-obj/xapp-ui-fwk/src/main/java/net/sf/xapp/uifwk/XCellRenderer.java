/*
 *
 * Date: 2010-dec-13
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;



import javax.swing.*;
import java.awt.*;

public class XCellRenderer<T> extends XPane implements ListCellRenderer
{
    protected T data;
    protected boolean selected;
    private int cellHeight = 30;
    protected int width;
    protected int index;
    protected JScrollPane containingScrollpane;

    public XCellRenderer(int width, JScrollPane containingScrollpane)
    {
        this.containingScrollpane = containingScrollpane;
        this.width = width;
        setDefaultAlpha(1);

    }

    public XCellRenderer<T> setCellHeight(int cellHeight)
    {
        this.cellHeight = cellHeight;
        return this;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        this.data = (T) value;
        this.selected = isSelected;
        this.index = index;
        setSize(list.getWidth(), cellHeight);
        newData(data);
        return this;
    }

    @Override
    protected void paintPane(Graphics2D g)
    {
        Color c = selected ? Color.yellow : Color.white;
        g.setPaint(new GradientPaint(0,0, c, 0, cellHeight, Color.gray));
        g.fillRoundRect(0,0,getVisibleWidth()-2, cellHeight-1, cellHeight, cellHeight);
        g.setColor(Color.gray);
        //g.drawRoundRect(0,0,getVisibleWidth()-3, cellHeight-2, 10,10);


        if (data !=null)
        {
            render(g, data);
        }

    }

    public int getVisibleWidth()
    {
        boolean isVerticalBarVisible = containingScrollpane != null &&
                containingScrollpane.getVerticalScrollBar().isVisible();
        if(isVerticalBarVisible)
        {
            return  width-containingScrollpane.getVerticalScrollBar().getWidth();
        }
        else
        {
            return width;
        }
    }

    protected void newData(T data)
    {

    }
    protected void render(Graphics2D g, T data)
    {
        g.setColor(Color.black);
        int h = g.getFontMetrics().getHeight() - 4;
        g.drawString(data.toString(), 10, getHeight()/2+h/2);
    }

    public void setScrollPane(JScrollPane sp)
    {
        containingScrollpane = sp;
    }
}
