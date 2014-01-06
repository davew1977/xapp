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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Linked list that has a max size, when adding and item to a "full" list the first item will be removed
 */
public class FixedSizedList<T>
{
    private int m_limit;
    private LinkedList<T> m_items;

    public FixedSizedList(int limit)
    {
        m_limit = limit;
        m_items = new LinkedList<T>();
    }

    /**
     * @return a copy of the internal list
     */
    public List<T> list()
    {
        return new ArrayList<T>(m_items);
    }

    public void add(T item)
    {
        if(m_items.size()>=m_limit)
        {
            m_items.removeFirst();
        }
        m_items.addLast(item);
    }

    public T get(int index)
    {
        return m_items.get(index);
    }

    public int size()
    {
        return m_items.size();
    }
}
