/*
 *
 * Date: 2010-aug-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import net.sf.xapp.net.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates a list of sublists and /or raw data
 * provides an api to get and set values easily
 */
public class LispObj
{
    private List content;

    public LispObj()
    {
        this(new ArrayList());
    }
    public LispObj(List content)
    {
        this.content = content;
    }

    public LispObj(String data)
    {
        this(StringUtils.parse(data));
    }

    public Object itemAt(int index)
    {
        return content.get(index);
    }

    public String get(int... indexes)
    {
        return (String) getObj(indexes);
    }

    public List getList(int... indexes)
    {
        return (List) getObj(indexes);
    }

    public LispObj subTree(int... indexes)
    {
        return new LispObj(getList(indexes));
    }

    public Object getObj(int... indexes)
    {
        List data = content;
        for (int i = 0; i < indexes.length; i++)
        {
            int index = indexes[i];
            if (i < indexes.length - 1)
            {
                data = (List) data.get(index);
            }
            else
            {
                return data.get(index);
            }
        }
        return data;
    }

    public void insert(String value, int... indexes)
    {
        set(value, Op.INSERT, indexes);
    }

    public void insert(LispObj value, int... indexes)
    {
        set(value.content, Op.INSERT, indexes);
    }

    public void remove(int... indexes)
    {
        set(null, Op.REMOVE, indexes);
    }

    public void set(String value, int... indexes)
    {
        set(value, Op.SET, indexes);
    }

    public void set(List value, int... indexes)
    {
        set(value, Op.SET, indexes);
    }

    public void set(LispObj value, int... indexes)
    {
        set(value.content, indexes);
    }

    public void removeLast(int... indexes)
    {
        int[] sub = Arrays.copyOfRange(indexes, 0, indexes.length);
        List list = getList(sub);
        list.remove(list.size()-1);
    }

    public void add(String value, int... indexes)
    {
        int[] sub = Arrays.copyOfRange(indexes, 0, indexes.length);
        getList(sub).add(value);
    }

    public void add(LispObj value, int... indexes)
    {
        int[] sub = Arrays.copyOfRange(indexes, 0, indexes.length);
        getList(sub).add(value.content);
    }

    public void set(Object value, Op op, int... indexes)
    {
        int[] sub = Arrays.copyOfRange(indexes, 0, indexes.length - 1);
        List list = getList(sub);
        int targetIndex = indexes[indexes.length - 1];
        if(targetIndex<0) //take from end
        {
            targetIndex = list.size() + targetIndex;
        }
        switch (op)
        {
            case INSERT:
                list.add(targetIndex, value);
                break;
            case SET:
                list.set(targetIndex, value);
                break;
            case REMOVE:
                list.remove(targetIndex);
                break;
        }
    }

    public String serialize()
    {
        return StringUtils.serialize(content);
    }

    @Override
    public String toString()
    {
        return serialize();
    }

    public int size()
    {
        return content.size();
    }

    public boolean isListAt(int i)
    {
        return itemAt(i) instanceof List;
    }

    public List getContent()
    {
        return content;
    }

    private enum Op
    {
        INSERT, SET, REMOVE
    }
}