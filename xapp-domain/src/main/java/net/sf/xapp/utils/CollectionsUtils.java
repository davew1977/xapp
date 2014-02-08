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

import java.util.Collection;
import java.util.List;
import java.util.Random;

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
}
