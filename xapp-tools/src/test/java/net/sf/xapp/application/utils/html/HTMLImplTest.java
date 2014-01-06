package net.sf.xapp.application.utils.html;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.awt.*;

/**
 * HTMLImpl Tester.
 *
 * @author <Authors name>
 * @since <pre>05/08/2009</pre>
 * @version 1.0
 */
public class HTMLImplTest extends TestCase 
{

    public HTMLImplTest(String name) 
    {
        super(name);
    }

    public void testTable()
    {
        HTMLImpl hi = new HTMLImpl();
        hi.table();
        hi.table();
        assertEquals("<table border=\"0\"></table><table border=\"0\">",hi.html());
        hi = new HTMLImpl();
        hi.table().tr().td().td().p("hello");
        assertEquals("<table border=\"0\"><tr><td></td><td></td></tr></table><p>hello</p>",hi.html());
        hi = new HTMLImpl();
        hi.table().tr().tr().td().td().p("hello");
        assertEquals("<table border=\"0\"><tr></tr><tr><td></td><td></td></tr></table><p>hello</p>",hi.html());
        hi = new HTMLImpl();
        hi.tdBgColor(Color.RED).table().tr().td().td().p("hello");
        assertEquals("<table border=\"0\"><tr><td bgColor=\"#ff0000\"></td><td bgColor=\"#ff0000\"></td></tr></table><p>hello</p>",hi.html());
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
        return new TestSuite(HTMLImplTest.class);
    }
}
