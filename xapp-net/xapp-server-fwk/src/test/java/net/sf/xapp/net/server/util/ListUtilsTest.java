package net.sf.xapp.net.server.util;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static net.sf.xapp.net.server.util.ListUtils.pick;

import static org.junit.Assert.*;

public class ListUtilsTest {

    @Test
    public void testPick() throws Exception
    {
        List<String> src = asList("a", "b", "c", "d", "8282", "kks");
        assertEquals(asList("a"), pick(src, null, 1));
        assertEquals(src, pick(src, null, 6));
        assertEquals(asList("c", "d"), pick(src, "b", 2));
        assertEquals(asList("c", "d", "8282"), pick(src, "b", 3));
        assertEquals(asList("8282", "kks"), pick(src, "d", 3));
    }
}
