package net.sf.xapp.net.server.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleCache<K,V> extends LinkedHashMap<K,V>
{
    private final int MAX_ENTRIES;

    public SimpleCache(int MAX_ENTRIES)
    {
        this.MAX_ENTRIES = MAX_ENTRIES;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return size()>MAX_ENTRIES;
    }
}
