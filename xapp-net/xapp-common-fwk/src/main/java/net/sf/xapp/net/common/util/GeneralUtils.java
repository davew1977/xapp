/*
 *
 * Date: 2010-nov-02
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneralUtils
{
    public static boolean objEquals(Object oldVal, Object newVal)
    {
        return oldVal == null && newVal == null || oldVal != null && oldVal.equals(newVal);
    }

    public static <T> List<List<T>> distribute(List<T> src, int maxPerUnit)
    {
        return distribute(src, maxPerUnit, new Random());
    }
    public static <T> List<List<T>> distribute(List<T> src, int maxPerUnit, Random r)
    {
        List<T> items = new ArrayList<T>(src);
        //we start enough single table sngs
        int numberOfItems = items.size();
        int tablesNeeded = MathUtils.divideAndRoundUp(numberOfItems, maxPerUnit);
        //distribute players
        List<List<T>> result = new ArrayList<List<T>>();
        for(int i=0; i<tablesNeeded; i++)
        {
            result.add(new ArrayList<T>());
        }
        for(int i=0; i<numberOfItems; i++)
        {
            int tableIndex = i % tablesNeeded;
            result.get(tableIndex).add(removeRandomItem(r, items));
        }
        return result;
    }


    public static <T> T removeRandomItem(Random r, List<T> list)
    {
        return list.remove(r.nextInt(list.size()));
    }
}
