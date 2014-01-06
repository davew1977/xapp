package net.sf.xapp.application.editor.widgets;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * LiveTemplate Tester.
 *
 * @author <Authors name>
 * @since <pre>07/22/2009</pre>
 * @version 1.0
 */
public class LiveTemplateTest extends TestCase 
{
    public LiveTemplateTest(String name) 
    {
        super(name);
    }

    public void testLiveTemplate()
    {
        LiveTemplate t = new LiveTemplate("this is a live template");
        t.reset(0);
        assertEquals(23 , t.nextCaretIndex());
        t = new LiveTemplate("this is a live $0 template");
        t.reset(0);
        assertEquals(15, t.nextCaretIndex());
        assertEquals("this is a live  template", t.getInsertion());
        t = new LiveTemplate("this$0 is a $1live $2 template");
        t.reset(0);
        assertEquals("this is a live  template", t.getInsertion());
        assertEquals(4,t.nextCaretIndex());
        assertEquals(10,t.nextCaretIndex());
        t = new LiveTemplate("this$1 is a $0live $3 templat$2e");
        t.reset(0);
        assertEquals(Arrays.asList(10, 4, 23, 15), t.caretIndexes());
        assertTrue(t.hasMore());
        assertEquals(10, t.nextCaretIndex());
        assertTrue(t.hasMore());
        assertEquals(4, t.nextCaretIndex());
        assertTrue(t.hasMore());
        assertEquals(23, t.nextCaretIndex());
        assertTrue(t.hasMore());
        assertEquals(15, t.nextCaretIndex());
        assertFalse(t.hasMore());
        t = new LiveTemplate("this $0 will be $1 inserted $2 hello");
        t.reset(0);
        System.out.println(t.caretIndexes());
    }

    public void testInsert()
    {
        LiveTemplate t;
        t = new LiveTemplate("this is a live $0 template");
        t.reset(5);
        t.nextCaretIndex();
        assertTrue(t.textInserted(20, "hello"));
        assertEquals("this is a live  template",t.getInsertion()); //insertion is immutable
        assertEquals("this is a live hello template",t.getContent()); //content is mutable
        assertEquals("hello", t.getSubs()[0]);
        assertEquals(15, (int) t.getCaretSettings().get(0));
        t.reset(0);
        assertEquals("", t.getSubs()[0]);

        t = new LiveTemplate("this is $0 a live $1 template");

        t.reset(0);
        t.nextCaretIndex();
        assertFalse(t.textInserted(7,"boo"));
        assertFalse(t.textInserted(9,"boo"));
        assertEquals(8, (int) t.getCaretSettings().get(0));
        assertEquals(16, (int) t.getCaretSettings().get(1));
        assertTrue(t.textInserted(8,"boo"));
        assertEquals(8, t.indexRange()[0]);
        assertEquals(3, t.indexRange()[1]);
        assertEquals(8, (int) t.getCaretSettings().get(0));
        assertEquals(19, (int) t.getCaretSettings().get(1));
        assertFalse(t.textInserted(7,""));
        assertTrue(t.textInserted(8,""));
        assertTrue(t.textInserted(9,""));
        assertTrue(t.textInserted(10,""));
        assertTrue(t.textInserted(11,""));
        assertFalse(t.textInserted(12,""));
        t.nextCaretIndex();
        assertFalse(t.textInserted(8,""));
        assertTrue(t.textInserted(19,""));
        assertFalse(t.textInserted(18,""));
        assertFalse(t.textInserted(20,""));
        assertFalse(t.hasMore());
        t.reset(0);
        assertEquals(8, (int) t.getCaretSettings().get(0));
        assertEquals(16, (int) t.getCaretSettings().get(1));

    }

    public void testRemove()
    {
        LiveTemplate t;
        t = new LiveTemplate("this is a live $0 templa$1te");
        t.reset(5);
        t.nextCaretIndex();
        assertFalse(t.textRemoved(20, 5));
        assertFalse(t.textRemoved(20, 1));
        assertTrue(t.textRemoved(20, 0));
        assertTrue(t.textInserted(20, "hello"));
        assertFalse(t.textRemoved(20, 6));
        assertTrue(t.textRemoved(20, 5));
        assertEquals("this is a live  template", t.getContent());
        assertTrue(t.textInserted(20, "hello"));
        assertEquals("this is a live hello template", t.getContent());
        assertEquals(27, (int) t.getCaretSettings().get(1));
        assertTrue(t.textRemoved(24, 1));
        assertEquals("this is a live hell template", t.getContent());
        assertFalse(t.textRemoved(24, 1));
        assertEquals(26, (int) t.getCaretSettings().get(1));
    }
}
