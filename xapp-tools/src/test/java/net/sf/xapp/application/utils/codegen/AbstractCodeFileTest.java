package net.sf.xapp.application.utils.codegen;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * AbstractCodeFile Tester.
 *
 * @author <Authors name>
 * @since <pre>10/15/2009</pre>
 * @version 1.0
 */
public class AbstractCodeFileTest extends TestCase 
{
    public AbstractCodeFileTest(String name) 
    {
        super(name);
    }

    public void testFindMethod()
    {
        CodeFile cf = new JavaFile(null);
        cf.setName("Doo");
        cf.method("foo", "bar", "int f","boolean g");

        Method method = cf.getMethod("foo", "int f", "boolean g");
        assertNotNull(method);
        method = cf.getMethod("foo");
        assertNotNull(method);

        cf.field("String", "grum", Access.READ_WRITE);
        cf.constructor();
        Method accessor = cf.getAccessor("grum");
        Method modifier = cf.getModifier("grum");
        Method defaultConstructor = cf.getDefaultConstructor();
        assertEquals("Doo", defaultConstructor.getName());
        assertEquals("getGrum", accessor.getName());
        assertEquals("setGrum", modifier.getName());

        Exception ex=null;
        try
        {
            method = cf.getMethod("foo", "int f");
        }
        catch (RuntimeException e)
        {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals("method foo(int f) not found in Doo.java", ex.getMessage());

    }
}
