/*
 *
 * Date: 2010-dec-13
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;





import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class XComboBox<T> extends JComboBox
{

    public XComboBox(T items[])
    {
        this(items, new XCellRenderer(100, null));
    }
    public XComboBox(T items[], final XCellRenderer<T> renderer)
    {
        super(items);
        setSize(100, 20);
        setFont(Font.decode("Tahoma-10"));
        setOpaque(false);
        setRenderer(renderer.setCellHeight(20));
        setUI(new BasicComboBoxUI()
        {
            @Override
            public void paintCurrentValueBackground(Graphics _g, Rectangle bounds, boolean hasFocus)
            {
                Graphics2D g = (Graphics2D) _g;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c = hasFocus ? Color.yellow : Color.white;
                g.setPaint(new GradientPaint(0, 0, c, 0, bounds.height, Color.gray));
                _g.fillRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 20, 20);
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus)
            {
                T selected = XComboBox.this.getSelected();
                if (selected != null)
                {
                    g.setColor(Color.black);
                    int h = g.getFontMetrics().getHeight() - 4;
                    g.drawString(selected.toString(), 10, bounds.height / 2 + h / 2);
                }
                decorateCurrentSelected((Graphics2D) g.create(bounds.x, bounds.y, bounds.width, bounds.height), selected);
            }

            @Override
            protected JButton createArrowButton()
            {
                return new XButton("")
                {
                    @Override
                    public void paintComponent(Graphics g)
                    {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setPaint(new GradientPaint(0,0,Color.lightGray, 0,20, Color.darkGray));
                        g2.fillPolygon(new int[]{5,15,10}, new int[]{7,7,15}, 3);
                    }
                };
            }

            @Override
            protected ComboPopup createPopup()
            {


                BasicComboPopup basicComboPopup = new BasicComboPopup(XComboBox.this)
                {
                    @Override
                    protected JScrollPane createScroller()
                    {
                        JScrollPane sp = new XScrollPane(list);
                        sp.setHorizontalScrollBar(null);
                        sp.getVerticalScrollBar().setOpaque(true);
                        renderer.setScrollPane(sp);

                        return sp;
                    }
                };
                basicComboPopup.setBorder(new EmptyBorder(0,0,0,0));
                basicComboPopup.setOpaque(false);
                basicComboPopup.getList().setOpaque(false);
                return basicComboPopup;
            }
        });

    }

    public XComboBox()
    {
        this((T[]) new Object[]{});
    }

    public XComboBox<T> location(int x, int y)
    {
        setLocation(x, y);
        return this;
    }

    public XComboBox<T> size(int x, int y)
    {
        setSize(x,y);
        return this;
    }

    public T getSelected()
    {
        return (T) getSelectedItem();
    }

    public void addItemListener(final Object listener, final String method, final Object... args)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method, args);
        addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED || getSelected()==null)
                {
                    ReflectionUtils.call(listener, method, args);
                }
            }
        });
    }

    public void decorateCurrentSelected(Graphics2D g, T data)
    {

    }
}
