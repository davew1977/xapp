/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.memdb;

import net.sf.xapp.net.server.lobby.types.Clause;
import net.sf.xapp.net.server.lobby.types.QueryData;

import java.util.*;

public class CollaboratorDatabase<T> implements Database<T>
{
    private PropertyInspector<T> propertyInspector;
    private List<String> properties;
    private Map<String, Map<String, Set<T>>> data;
    private Map<String, T> allItems;

    public CollaboratorDatabase(PropertyInspector<T> propertyInspector, List<String> properties)
    {
        this.propertyInspector = propertyInspector;
        this.properties = properties;
        data = new HashMap<String, Map<String, Set<T>>>();
        for (String property : properties)
        {
            data.put(property, new HashMap<String, Set<T>>());
        }
        allItems = new HashMap<String, T>();
    }

    @Override
    public void store(String key, T item)
    {
        for (String property : properties)
        {
            String propValue = propertyInspector.getValue(item, property);
            Map<String, Set<T>> propSet = data.get(property);
            Set<T> set = propSet.get(propValue);
            if (set == null)
            {
                set = new HashSet<T>();
                propSet.put(propValue, set);
            }
            set.add(item);
        }
        allItems.put(key, item);
    }

    @Override
    public T remove(String key)
    {
        T item = allItems.remove(key);
        if (item != null)
        {
            for (String property : properties)
            {
                String propValue = propertyInspector.getValue(item, property);
                data.get(property).get(propValue).remove(item);
            }
        }
        return item;
    }

    @Override
    public Set<T> find(String encodedQuery)
    {
        return find(new QueryData().deserialize(encodedQuery));
    }

    @Override
    public Set<T> find(QueryData query)
    {
        List<Clause> clauses = query.getClauses();
        List<String> keys = query.getKeys();
        Set<T> results = new HashSet<T>(allItems.values());
        for (Clause clause : clauses)
        {
            String prop = clause.getProperty();
            Map<String, Set<T>> games = data.get(prop);
            Set<T> clauseResults = new HashSet<T>();
            for (String value : clause.getValues())
            {
                Set<T> matches = games.get(value);
                if (matches != null)
                {
                    clauseResults.addAll(matches);
                }
            }
            results.retainAll(clauseResults);
        }
        //if there are no clauses, then keys are used, if keys are empty then return all
        if(clauses.isEmpty() && !keys.isEmpty())
        {
            results = new HashSet<T>();
            for (String key : keys)
            {
                results.add(allItems.get(key));
            }
        }
        //sometimes we want an empty query to retrieve all, sometimes we want it to retrieve nothing
        else if(clauses.isEmpty() && keys.isEmpty() && !query.isRetrieveAll())
        {
            results.clear();
        }

        return results;
    }

    @Override
    public T findByKey(String key)
    {
        return allItems.get(key);
    }

    @Override
    public int size()
    {
        return allItems.size();
    }
}
