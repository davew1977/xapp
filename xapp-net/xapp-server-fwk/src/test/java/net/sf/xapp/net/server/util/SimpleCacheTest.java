package net.sf.xapp.net.server.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleCacheTest {

    @Test
    public void testCache()
    {
        SimpleCache<String, String> cache = new SimpleCache<String, String>(4);
        cache.put("1","a");
        cache.put("2","b");
        cache.put("3","c");
        cache.put("4","d");

        assertEquals(4, cache.size());
        cache.put("5","e");
        assertEquals(4, cache.size());
        assertEquals("{2=b, 3=c, 4=d, 5=e}", cache.toString());
    }
}
