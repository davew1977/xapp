/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.utils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;

import static net.sf.xapp.utils.StringUtils.*;

public class StringUtilsTest extends TestCase
{
    public void testToCamelCase()
    {
        assertEquals("thisIsTheLife", toCamelCase("this is the life"));
        assertEquals("thisIsTheLife", toCamelCase("this is  The life"));
        assertEquals("thisIsTHELife", toCamelCase("this is  THE life"));
        assertEquals("", toCamelCase(""));
        assertEquals("T", toCamelCase("T"));
        assertEquals("kdjslLJKkjkdKJK", toCamelCase("kdjslLJKkjkdKJK"));
    }

     /*public void testToAsciiString()
     {
         assertEquals("\\u00e4", toAsciiString("ä"));
     }*/

    public void testInsert()
    {
        assertEquals("qwertyboouiop",insert("qwertyuiop",6,"boo"));
        assertEquals("booqwertyuiop",insert("qwertyuiop",0,"boo"));
        assertEquals("qwertyuiopboo",insert("qwertyuiop",10,"boo"));
    }

    public void testRemove()
    {
        assertEquals("qwertyuiop",remove("qwertyboouiop",6,3));
        assertEquals("qwertyuiop",remove("booqwertyuiop",0,3));
        assertEquals("qwertyuiop",remove("qwertyuiopboo",10,3));
    }

    public void testBreakLine()
    {
        assertEquals("short line", breakText("short line", "\n", 20));
        assertEquals("this is a\nlonger\nline", breakText("this is a longer line", "\n", 10));
        assertEquals("this is a<br/>longer<br/>line", breakText("this is a longer line", "<br/>", 10));
        assertEquals("this is a<br/>longer pin", breakText("this is a longer pin", "<br/>", 10));
        assertEquals("this is a<br/>longer pin", breakText("this is a longer pin ", "<br/>", 10));
        assertEquals("thisisalon<br/>gerline", breakText("thisisalongerline", "<br/>", 10));
    }

    public void testConvertToStringList()
    {
        assertEquals(Arrays.asList("Hello", "This", "is a list"), convertToStringList("Hello,This,is a list"));
        assertTrue(convertToStringList("").isEmpty());
    }

    public void testConvertToString()
    {
        assertEquals("Hello,This,is a list,", convertToString(Arrays.asList("Hello", "This", "is a list")));
        boolean nullPointer = false;
        try
        {
            convertToString(null);
        }
        catch (NullPointerException e)
        {
            nullPointer = true;
        }
        assertTrue(nullPointer);
        assertEquals("", convertToString(new ArrayList<String>()));
    }

    public void testParsePropertyString()
    {
        String propstring="prop1=hello;xxx=yyy;yum=boo";
        Map<String,String> map = parsePropertyString(propstring, ";");
        assertEquals(3, map.size());
        assertEquals("hello", map.get("prop1"));
        assertEquals("yyy", map.get("xxx"));
        assertEquals("boo", map.get("yum"));

        map = parsePropertyString(null, ",");
        assertNull(map);
        map = parsePropertyString("", ",");
        assertNull(map);

        map = parsePropertyString("a=2,b=3", ",");
        assertEquals("2", map.get("a"));
        assertEquals("3", map.get("b"));



    }


    public void testCamelToUpper()
    {
        assertEquals("JAVA_VARIABLE_CASE", camelToUpper("javaVariableCase"));
        assertEquals("CLASS_NAME", camelToUpper("ClassName"));
        String str = "ThisHasABIGAcronymInIt";
        System.out.println(str);
        assertEquals("THIS_HAS_ABIG_ACRONYM_IN_IT", camelToUpper(str));
    }
    
    public void testXMLRootTag()
    {
        assertEquals("foo", xmlRootTag("<foo>blah</foo>"));
        assertEquals("foo", xmlRootTag("<foo/>"));
        assertEquals("foo", xmlRootTag("<foo atr=\"ba\"/>"));
        assertEquals("foo", xmlRootTag("<foo atr=\"ba\">blah</foo>"));
    }

    public void testRemoveTokenAt()
    {
        assertEquals("a,b,d", removeTokenAt("a,b,c,d", ",", 2));
        assertEquals("a,b,c", removeTokenAt("a,b,c,d", ",", 3));
        assertEquals("a,b,c", removeTokenAt("a,b,c,d", ",", -1));
        assertEquals("a,b,d", removeTokenAt("a,b,c,d", ",", -2));
        assertEquals("", removeTokenAt("", ",", 0));
        assertEquals("", removeTokenAt(",", ",", 0));
    }
}
