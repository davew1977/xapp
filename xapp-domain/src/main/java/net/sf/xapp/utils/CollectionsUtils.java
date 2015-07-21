/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.utils;


import net.sf.xapp.objectmodelling.core.filters.ClassFilter;

import java.util.*;

public class CollectionsUtils
{
    public static boolean containsAny(Collection<?> source, Collection<?> candidates)
    {
        for (Object o : candidates)
        {
            if(source.contains(o))
            {
                return true;
            }
        }
        return false;
    }

    public static <T> T pickRandomItem(List<T> items)
    {
        return items.get(new Random().nextInt(items.size()));
    }
    
    public static <T> T removeRandomItem(Random r, List<T> list)
    {
        return list.remove(r.nextInt(list.size()));
    }


    public static <T> T next(List<T> items, T t) {
        int i = items.indexOf(t);
        return i!=-1 ? items.get((i + 1) % items.size()) : null;
    }
    public static <T> T previous(List<T> items, T t) {
        int i = items.indexOf(t);
        return i!=-1 ? items.get((i + items.size() - 1) % items.size()) : null;
    }

    public static <K,V> Set<V> lookup(Map<K,V> map, Collection<K> keys) {
        Set<V> result = new LinkedHashSet<V>();
        for (K key : keys) {
            V e = map.get(key);
            if (e != null) {
                result.add(e);
            }
        }
        return result;
    }


    public static <T> List<T> filter(Collection<? extends T> items, Filter<? super T> filter) {
        List<T> result = new ArrayList<T>();
        for (T item : items) {
            if(filter.matches(item)) {
                result.add(item);
            }
        }
        return result;
    }


    public static <T> List<T> filter(Collection<? super T> items, Class<T> filterClass) {
        return filter(items, filterClass, new Filter<T>() {
            @Override
            public boolean matches(T t) {
                return true;
            }
        });

    }
    public static <T> List<T> filter(Collection<? super T> items, Class<T> filterClass, Filter<? super T> filter) {
        List<T> result = new ArrayList<T>();
        for (Object item : items) {
            if(filterClass.isInstance(item) ) {
                T t = filterClass.cast(item);
                if (filter.matches(t)) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    public static <K, V> Map<K, List<V>> groupBy(Collection<? extends V> items, Grouping<K,V> grouping) {
        Map<K, List<V>> result = new java.util.LinkedHashMap<K, List<V>>();
        for (V item : items) {
            K group = grouping.getGroup(item);
            List<V> g = result.get(group);
            if(g==null) {
                g = new ArrayList<V>();
                result.put(group, g);
            }
            g.add(item);
        }
        return result;
    }

    public static <T> List<T> select(List<T> src, List<Integer> indexes) {
        List<T> result = new ArrayList<T>();
        for (Integer index : indexes) {
            result.add(src.get(index));
        }
        return result;
    }

    public static <T> T zeroOrOne(List<? extends T> items) {
        return items==null || items.isEmpty() ? null : items.get(0);
    }
}
