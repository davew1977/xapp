package net.sf.xapp.uifwk;


import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.xapp.uifwk.anim.Effect;
import net.sf.xapp.uifwk.anim.Scene;


/**
 * like a JList, but using "real" components instead of a renderer
 */
public class SimpleList<T extends XPane> extends XPane
{
    private final Map<String, T> cache;
    private final int gap;

    public SimpleList(int gap)
    {
        this.gap = gap;
        cache = new LinkedHashMap<String, T>();
    }

    public void init(List<T> rows)
    {
        cache.clear();
        removeAll();
        int h = 0;
        for (T pane : rows)
        {
            pane.setLocation(0, h);
            add(pane, getComponentCount());
            h += pane.getHeight() + gap;
        }
    }

    public void addRow(T newRow)
    {
        addRow(newRow, getComponentCount());
    }

    public void addRow(T newRow, int index)
    {
        int newHeight = newRow.getHeight();
        newRow.setVisible(false);
        if (index > 0) //newrow must appear where the current comp at index is
        {
            Component compBefore = getComponent(index - 1);
            newRow.setLocation(0, compBefore.getY() + compBefore.getHeight());
        } //otherwise assume newrow is at 0,0
        //adjust height for new row

        //animate change
        Scene s = newScene();
        //move all rows lower than the new one, downward
        for (int i = index; i < getComponentCount(); i++)
        {
            XPane row = (XPane) getComponent(i);
            s.newSequence().moveTo(row,
                    row.getX(),
                    row.getY() + newHeight, 500, Effect.BOTH);
        }
        add(newRow, index);
        s.show(newRow);
        s.fadeIn(newRow);
        s.start();
    }

    private Component add(T comp, int index)
    {
        Component a = super.add(comp, index);
        String key = getKey(comp);
        T exists = cache.put(key, comp);
        assert exists == null;
        //set new size
        int h = 0;
        for (T t : cache.values())
        {
            h+=t.getHeight() + gap;
        }
        setSize(getWidth(), h);
        return a;
    }

    private String getKey(T comp)
    {
        return (String) ReflectionUtils.call(comp, "getKey");
    }

    public void remove(String key)
    {
        cache.remove(key);
        init(new ArrayList<T>(cache.values()));
    }

    public int getNoItems()
    {
        return getComponentCount();
    }

    public T get(String key)
    {
        return cache.get(key);
    }
}
