package net.sf.xapp.net.common.framework;

import junit.framework.TestCase;

import static net.sf.xapp.net.common.util.MathUtils.*;

public class MathUtilsTest extends TestCase
{
    public void testIsWhole() throws Exception
    {
        assertTrue(isWhole(Math.sqrt(9)));
        assertFalse(isWhole(Math.sqrt(8)));
    }

    public void testIsOdd() throws Exception
    {
        assertTrue(isOdd(1));
        assertFalse(isOdd(2));
        assertTrue(isOdd(3));
    }

    public void testFloor()
    {
        assertEquals(16, floor(17,4));
        assertEquals(20, floor(22,4));
        assertEquals(24, floor(24,4));
    }

    public void testNextPrevOdd()
    {
        assertEquals(1, previousOdd(1.1));
        assertEquals(1, previousOdd(2.1));
        assertEquals(3, nextOdd(2.1));
        assertEquals(3, nextOdd(1.1));
        assertEquals(3, nextOdd(1));
        assertEquals(5, nextOdd(3));
        assertEquals(3, previousOdd(3));
    }
}
