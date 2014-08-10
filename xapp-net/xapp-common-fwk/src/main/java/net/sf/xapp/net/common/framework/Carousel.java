/*
 *
 *
 *
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import net.sf.xapp.utils.Filter;

import java.util.ArrayList;
import java.util.List;

public class Carousel<T> extends ArrayList<T>
{
    private int index;

    public Carousel(List<T> list, T startObj)
    {
        super(list);
        setStartIndex(startObj);
    }

    public Carousel()
    {
        this(new ArrayList<T>(), null);
    }

    public T next()
    {
        return offs(1);
    }

    private T offs(int delta)
    {
        index = (index + size() + delta) % size();
        return get(index);
    }

    public T previous()
    {
        return offs(-1);
    }

    public T previous(T startObj, Filter<T> filter)
    {
        return offs(indexOf(startObj), filter, 1, -1);
    }
    public T previous(int startIndex, Filter<T> filter)
    {
        return offs(startIndex, filter, 1, -1);
    }
    public T next(int startIndex, Filter<T> filter)
    {
        return offs(startIndex, filter, 1, 1);
    }
    public T next(int startIndex, Filter<T> filter, int steps)
    {
        return offs(startIndex, filter, steps, 1);
    }
    public T next(Filter<T> filter)
    {
        return next(filter, 1);
    }
    public T next(Filter<T> filter, int steps)
    {
        return offs(filter, steps, 1);
    }
    public T previous(Filter<T> filter, int steps)
    {
        return offs(filter, steps, -1);
    }
    public T previous(Filter<T> filter)
    {
        return offs(filter, 1, -1);
    }

    private T offs(int startIndex, Filter<T> filter, int steps, int delta)
    {
        setStartIndex(startIndex);
        return offs(filter, steps, delta);
    }
    private T offs(Filter<T> filter, int steps, int delta)
    {
        for (int i = 0; i < this.size(); i++)
        {
            T obj = offs(delta);
            if(filter.matches(obj) && --steps==0)
            {
                return obj;
            }
        }
        return null;
    }

    public void setStartIndex(T obj)
    {
        setStartIndex(indexOf(obj));
    }

    public void setStartIndex(int index)
    {
        this.index = index;
    }

    public T previous(T item)
    {
        setStartIndex(item);
        return previous();
    }
}
