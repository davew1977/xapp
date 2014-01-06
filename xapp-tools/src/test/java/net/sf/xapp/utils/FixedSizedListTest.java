/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.utils;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * FixedSizedList Tester.
 *
 * @author <Authors name>
 * @since <pre>09/03/2009</pre>
 * @version 1.0
 */
public class FixedSizedListTest extends TestCase 
{
    public FixedSizedListTest(String name) 
    {
        super(name);
    }

    public void testAdd()
    {
        FixedSizedList<String> s = new FixedSizedList<String>(5);
        s.add("1");
        s.add("2");
        s.add("3");
        s.add("4");
        s.add("5");
        assertEquals("1", s.get(0));
        assertEquals("2", s.get(1));
        assertEquals("3", s.get(2));
        assertEquals("4", s.get(3));
        assertEquals("5", s.get(4));
        s.add("6");
        assertEquals("2", s.get(0));
        assertEquals(5, s.size());
    }
}
