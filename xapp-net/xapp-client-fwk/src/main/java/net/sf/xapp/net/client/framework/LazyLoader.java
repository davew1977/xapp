package net.sf.xapp.net.client.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LazyLoader<T>
{
    private Map<String, T> cache;
    private Map<String, List<Callback>> callbacks;

    public LazyLoader()
    {
        cache = new HashMap<String, T>();
        callbacks = new HashMap<String, List<Callback>>();
    }

    public T load(String key, String callbackMethod, Object target)
    {
        Callback callback = new Callback(callbackMethod, target);
        T t = cache.get(key);
        if(t!=null)
        {
            callback.call(t);
        }
        else
        {
            callbacks(key).add(callback);
        }
        return t;
    }

    public void put(String key, T obj)
    {
        cache.put(key, obj);
        List<Callback> callbackList = callbacks.remove(key);
        if(callbackList!=null)
        {
            for (Callback callback : callbackList)
            {
                callback.call(obj);
            }
        }
    }

    private List<Callback> callbacks(String key)
    {
        List<Callback> callbackList = callbacks.get(key);
        if(callbackList==null)
        {
            callbackList = new ArrayList<Callback>();
            callbacks.put(key, callbackList);
        }
        return callbackList;
    }

}