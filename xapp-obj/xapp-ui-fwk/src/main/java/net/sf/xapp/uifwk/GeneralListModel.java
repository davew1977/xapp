/*
 *
 * Date: 2010-sep-29
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GeneralListModel<T> extends AbstractListModel
{
    List<T> items;

    public GeneralListModel(List<T> items)
    {
        this.items = items;
    }

    public GeneralListModel()
    {
        this(new ArrayList<T>());
    }

    @Override
    public int getSize()
    {
        return items.size();
    }

    @Override
    public Object getElementAt(int index)
    {
        return items.get(index);
    }

    public void add(T item)
    {
        items.add(item);
        fireIntervalAdded(this, items.size()-1, items.size());
    }

    public void remove(T item)
    {
        remove(items.indexOf(item));
    }

    private void remove(int i)
    {
        items.remove(i);
        fireIntervalRemoved(this, i,i);
    }

    public void clear()
    {
        this.items.clear();
        fireAllChanged();
    }

    public void setItems(List<T> items)
    {
        int preSize = this.items.size();
        this.items.clear();
        this.items.addAll(items);
        fireContentsChanged(this, 0, Math.max(items.size(), preSize));
    }

    public List<T> getItems()
    {
        return items;
    }

    public void rowUpdated(T lobbyEntity)
    {
        int i = items.indexOf(lobbyEntity);
        fireContentsChanged(this, i,i);
    }

    public boolean contains(T value)
    {
        return items.contains(value);
    }

    public void fireAllChanged()
    {
        fireContentsChanged(this, 0, items.size());
    }

    public void sort(Comparator<T> comparator)
    {
        Collections.sort(items, comparator);
        fireAllChanged();
    }
}
