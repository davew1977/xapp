package net.sf.xapp.net.common.framework;
import net.sf.xapp.net.common.framework.LispObj;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.List;

/**
 * Created by dwebber
 */
public class LispObjTest {

    @Test
    public void testGet()
    {
        String data = "[S-1-1000,[sng,100,0,0,REAL_MONEY,USD,10,10,true,CASH,0,0,[],,HOLDEM,0,],[[1,2,dave,],],[],[],[],]";
        LispObj o = new LispObj(data);

        assertEquals(data, o.serialize());

        assertEquals("sng", o.get(1,0));
        assertEquals("dave", o.get(2,0,2));
        o.set("boo", 2,0,2);
        assertEquals("boo", o.get(2,0,2));
        o.insert("hoo", 2,0,3); //add a value
        assertEquals("hoo", o.get(2,0,3));
        o.insert("xxx", 2,0,4); //add another value
        assertEquals("xxx", o.get(2,0,4));

        o.set("sas", 1); //actually wipes away some of the structure
        assertEquals("[S-1-1000,sas,[[1,2,boo,hoo,xxx,],],[],[],[],]", o.serialize());
        o.insert("kip", 2,1);
        assertEquals("[S-1-1000,sas,[[1,2,boo,hoo,xxx,],kip,],[],[],[],]", o.serialize());

        o.subTree(2,0).set("99", 0);
        assertEquals("99", o.get(2,0,0));
        assertEquals("[S-1-1000,sas,[[99,2,boo,hoo,xxx,],kip,],[],[],[],]", o.serialize());
        o.remove(2,0,0);
        assertEquals("[S-1-1000,sas,[[2,boo,hoo,xxx,],kip,],[],[],[],]", o.serialize());

        List list = o.getList();
        assertEquals(6, list.size());


        o.add("HELLO",5);
        o.add("jim",2,0);
        o.add("at the end");
        o.add(new LispObj("[1,2]"));
        assertEquals("[S-1-1000,sas,[[2,boo,hoo,xxx,jim,],kip,],[],[],[HELLO,],at the end,[1,2,],]", o.serialize());
        System.out.println(o.serialize());
        o.removeLast();
        o.removeLast();
        o.removeLast(5);
        o.removeLast(2,0);
        assertEquals("[S-1-1000,sas,[[2,boo,hoo,xxx,],kip,],[],[],[],]", o.serialize());
    }
}
