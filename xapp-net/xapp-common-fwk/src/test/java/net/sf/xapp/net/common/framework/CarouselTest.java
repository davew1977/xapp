package net.sf.xapp.net.common.framework;

import junit.framework.TestCase;
import net.sf.xapp.net.common.framework.Carousel;
import net.sf.xapp.net.common.framework.Matcher;

import java.util.Arrays;
import java.util.List;

/**
 * Carousel Tester.
 *
 * @author <Authors name>
 * @since <pre>10/04/2010</pre>
 * @version 1.0
 */
public class CarouselTest extends TestCase 
{
    public void testOne()
    {
        List<String> list = Arrays.asList("sam","kenny","lynda","spike");

        Carousel<String> carousel = new Carousel<String>(list, "kenny");
        assertEquals("lynda", carousel.next());
        assertEquals("spike", carousel.next());
        assertEquals("sam", carousel.next());
        assertEquals("kenny", carousel.next());
        assertEquals("lynda", carousel.next());
        carousel.setStartIndex("kenny");
        assertEquals("lynda", carousel.next());
        assertEquals("kenny", carousel.previous());
        assertEquals("sam", carousel.previous());
        assertEquals("spike", carousel.previous());


        Matcher<String> startsWithS = new Matcher<String>()
        {
            @Override
            public boolean matches(String obj)
            {
                return obj.startsWith("s");
            }
        };
        assertEquals("sam", carousel.previous("spike", startsWithS));
        assertEquals("spike", carousel.previous(startsWithS));
        assertEquals("sam", carousel.next(startsWithS));
        assertEquals("sam", carousel.next(startsWithS, 2));
    }
}
