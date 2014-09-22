/*
 *
 * Date: 2010-dec-13
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;



import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class XList<T> extends JList
{
    public XList()
    {
        setOpaque(false);
    }

    public XList(ListModel dataModel)
    {
        super(dataModel);
        setOpaque(false);
    }

    public XList(Object[] listData)
    {
        super(listData);
        setOpaque(false);
    }

    public XList(Vector<T> listData)
    {
        super(listData);
        setOpaque(false);
    }

    public T getSelected()
    {
        return (T) getSelectedValue();
    }

    public List<T> getSelectedItems()
    {
        return (List<T>) Arrays.asList(getSelectedValues());
    }

    public void addItemListener(final Object listener, final String method, final Object... args)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method, args);
        addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    ReflectionUtils.call(listener, method, args);
                }
            }
        });
    }

    public void addDoubleClickListener(final Object listener, final String method)
    {
        ReflectionUtils.checkMethodExists(listener.getClass(), method);
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent evt)
            {
                if (evt.getClickCount() == 2)
                { 
                    ReflectionUtils.call(listener, method);
                }
            }
        });

    }

    public void setItems(List<T> items)
    {
        setListData(new Vector<Object>(items));
    }

    public void setSelected(T item)
    {
        setSelectedValue(item, true);
    }

    public XList<T> size(int width, int height)
    {
        setSize(width, height);
        return this;
    }

    public XList<T> location(int x, int y)
    {
        setLocation(x, y);
        return this;
    }
}
