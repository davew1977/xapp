/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.memdb;

import net.sf.xapp.net.common.types.Clause;
import net.sf.xapp.net.common.types.ListOp;
import net.sf.xapp.net.common.types.QueryData;

import java.util.List;
import java.util.Set;

public class LiveQuery<T>
{
    private final QueryData queryData;
    private final Set<T> matches;
    private final MultiLiveQueryListener<T> listenerList;
    private final PropertyInspector<T> propertyInspector;

    public LiveQuery(QueryData queryData, Set<T> initialMatches, PropertyInspector<T> propertyInspector)
    {
        this.queryData = queryData;
        matches = initialMatches;
        this.propertyInspector = propertyInspector;
        listenerList = new MultiLiveQueryListener<T>();
    }

    public void addListener(LiveQueryListener<T> liveQueryListener)
    {
        listenerList.add(liveQueryListener);
    }

    public void removeListener(LiveQueryListener<T> liveQueryListener)
    {
        listenerList.remove(liveQueryListener);
    }

    public Set<T> getMatches()
    {
        return matches;
    }

    public void itemAdded(T item)
    {
        if(matches(item))
        {
            matches.add(item);
            listenerList.itemAdded(item);
        }
    }

    public void itemRemoved(T item)
    {
        if(matches(item))
        {
            matches.remove(item);
            listenerList.itemRemoved(item);
        }
    }

    public void itemChanged(T item, String propName, String value)
    {
        //TODO when we allow queries based on dynamic props, we should detect if the
        //TODO item is added or removed from this set
        if(matches.contains(item))
        {
            listenerList.itemChanged(item, propName, value);
        }
    }

    public void itemChanged(T item, String propName, int index, String value, ListOp listOp)
    {
        //TODO when we allow queries based on dynamic props, we should detect if the
        //TODO item is added or removed from this set
        if(matches.contains(item))
        {
            listenerList.itemChanged(item, propName, index, value, listOp);
        }
    }

    private boolean matches(T item)
    {
        List<Clause> clauses = queryData.getClauses();
        List<String> keys = queryData.getKeys();
        boolean fullMatch = keys.isEmpty();
        for (Clause clause : clauses)
        {
            String prop = clause.getProperty();
            boolean propMatch = false;
            for (String value : clause.getValues())
            {
                propMatch |= propertyInspector.getValue(item, prop).equals(value);
            }
            fullMatch &= propMatch;
        }
        for (String key : keys)
        {
            fullMatch|=propertyInspector.getValue(item, "key").equals(key);
        }
        return fullMatch;
    }
}
