/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.utils;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.List;
import java.util.Arrays;

/**
 * CollectionsUtils Tester.
 *
 * @author <Authors name>
 * @since <pre>04/27/2009</pre>
 * @version 1.0
 */
public class CollectionsUtilsTest extends TestCase 
{
    public CollectionsUtilsTest(String name) 
    {
        super(name);
    }

    public void testContainsAny()
    {
        List<String> src = Arrays.asList("this","is","a","list");
        List<String> candidate1 = Arrays.asList("this","is","a","list");
        List<String> candidate2 = Arrays.asList("is","other1","other2");
        List<String> candidate3 = Arrays.asList("other2","other1","other2");
        List<String> candidate4 = Arrays.asList("");
        List<String> candidate5 = Arrays.asList();
        assertTrue(CollectionsUtils.containsAny(src, candidate1));
        assertTrue(CollectionsUtils.containsAny(src, candidate2));
        assertFalse(CollectionsUtils.containsAny(src, candidate3));
        assertFalse(CollectionsUtils.containsAny(src, candidate4));
        assertFalse(CollectionsUtils.containsAny(src, candidate5));
    }

    public void setUp() throws Exception 
    {
        super.setUp();
    }

    public void tearDown() throws Exception 
    {
        super.tearDown();
    }
    
    public static Test suite() 
    {
        return new TestSuite(CollectionsUtilsTest.class);
    }
}
