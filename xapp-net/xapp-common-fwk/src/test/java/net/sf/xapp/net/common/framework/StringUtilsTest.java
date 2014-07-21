package net.sf.xapp.net.common.framework;

import junit.framework.TestCase;

import java.util.List;

import static net.sf.xapp.net.common.util.StringUtils.*;

/**
 * StringUtils Tester.
 *
 * @author <Authors name>
 * @since <pre>11/29/2010</pre>
 * @version 1.0
 */
public class StringUtilsTest extends TestCase 
{
    public StringUtilsTest(String name) 
    {
        super(name);
    }

    public void testFormatTime()
    {
        assertEquals("0:00:01", formatTime(1000));
        assertEquals("0:00:01", formatTime(1283));
        assertEquals("0:01:00", formatTime(60000));
        assertEquals("1:01:00", formatTime(3660000));
        int day = 1000 * 60 * 60 * 24;
        assertEquals("1 day, 0:00:00", formatTime(day));
        assertEquals("23:59:59", formatTime(day - 1));
        assertEquals("2 days, 0:00:00", formatTime(2 * day));
        assertEquals("1 week, 1 day, 0:00:00", formatTime(8 * day));
    }

    public void testDepth()
    {
        List obj = parse("[[[],[[]]]]");
        assertEquals(4, depth(obj));
        assertEquals(1, depth(parse("[]")));
        assertEquals(5, depth(parse("[[[[[]]]],[]]")));
    }
}
