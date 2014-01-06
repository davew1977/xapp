/*
 *
 * Date: 2010-okt-13
 * Author: davidw
 *
 */
package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;

import java.util.ArrayList;
import java.util.List;

public class ListSerializer<E> implements StringSerializer<List<E>>
{
    private StringSerializer<E> ss;

    public ListSerializer(StringSerializer<E> ss)
    {
        this.ss = ss;
    }

    @Override
    public List<E> read(String str)
    {
        ArrayList<E> result = new ArrayList<E>();
        if(str==null)
        {
            return result;
        }
        String[] items = str.split(",");
        for (String item : items)
        {
            result.add(ss.read(item));
        }
        return result;
    }

    @Override
    public String write(List<E> obj)
    {
        StringBuilder sb = new StringBuilder();
        for (E e : obj)
        {
            sb.append(ss.write(e));
        }
        return sb.toString();
    }

    @Override
    public String validate(String text)
    {
        return null;
    }
}
