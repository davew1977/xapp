/*
 *
 *
 *
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

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

    public T previous(T startObj, Matcher<T> matcher)
    {
        return offs(indexOf(startObj), matcher, 1, -1);
    }
    public T previous(int startIndex, Matcher<T> matcher)
    {
        return offs(startIndex, matcher, 1, -1);
    }
    public T next(int startIndex, Matcher<T> matcher)
    {
        return offs(startIndex, matcher, 1, 1);
    }
    public T next(int startIndex, Matcher<T> matcher, int steps)
    {
        return offs(startIndex, matcher, steps, 1);
    }
    public T next(Matcher<T> matcher)
    {
        return next(matcher, 1);
    }
    public T next(Matcher<T> matcher, int steps)
    {
        return offs(matcher, steps, 1);
    }
    public T previous(Matcher<T> matcher, int steps)
    {
        return offs(matcher, steps, -1);
    }
    public T previous(Matcher<T> matcher)
    {
        return offs(matcher, 1, -1);
    }

    private T offs(int startIndex, Matcher<T> matcher, int steps, int delta)
    {
        setStartIndex(startIndex);
        return offs(matcher, steps, delta);
    }
    private T offs(Matcher<T> matcher, int steps, int delta)
    {
        for (int i = 0; i < this.size(); i++)
        {
            T obj = offs(delta);
            if(matcher.matches(obj) && --steps==0)
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
