package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.TestEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * EnumListSerializer Tester.
 *
 * @author <Authors name>
 * @since <pre>06/09/2009</pre>
 * @version 1.0
 */
public class EnumListSerializerTest
{
    @Test()
    public void testSerialize()
    {
        EnumListSerializer e = new EnumListSerializer(TestEnum.class);
        assertEquals("", e.write(null));
        assertEquals("one,two,three,", e.write(Arrays.asList(TestEnum.one, TestEnum.two, TestEnum.three)));
        assertEquals("",e.write(new ArrayList<Enum>()));


        List<? extends Enum> enums = e.read("one,two,three");
        assertEquals(3,enums.size());
        assertEquals(TestEnum.one, enums.get(0));
        assertEquals(TestEnum.two, enums.get(1));
        assertEquals(TestEnum.three, enums.get(2));

        assertTrue(e.read(null).isEmpty());
        assertTrue(e.read("").isEmpty());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadEnumValue() {
        EnumListSerializer e = new EnumListSerializer(TestEnum.class);
        e.read("bogus"); //should throw exception

    }
}
