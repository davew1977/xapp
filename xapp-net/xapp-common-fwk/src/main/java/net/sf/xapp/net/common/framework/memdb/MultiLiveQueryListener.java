/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.memdb;

import net.sf.xapp.net.common.types.ListOp;

import java.util.ArrayList;

public class MultiLiveQueryListener<T> extends ArrayList<LiveQueryListener<T>> implements LiveQueryListener<T>
{
    @Override
    public void itemAdded(T item)
    {
        for (LiveQueryListener<T> listener : this)
        {
            listener.itemAdded(item);
        }
    }

    @Override
    public void itemRemoved(T item)
    {
        for (LiveQueryListener<T> listener : this)
        {
            listener.itemRemoved(item);
        }
    }

    public void itemChanged(T item, String propName, String value)
    {
        for (LiveQueryListener<T> listener : this)
        {
            listener.itemChanged(item, propName, value);
        }
    }

    public void itemChanged(T item, String propName, int index, String value,  ListOp listOp)
    {
        for (LiveQueryListener<T> listener : this)
        {
            listener.itemChanged(item, propName, index, value, listOp);
        }
    }
}
