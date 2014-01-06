package net.sf.xapp.application.editor.widgets;

import junit.framework.TestCase;
import net.sf.xapp.application.editor.text.TextEditor;

/**
 * TextEditor Tester.
 *
 * @author <Authors name>
 * @since <pre>05/19/2009</pre>
 * @version 1.0
 */
public class TextEditorTest extends TestCase 
{
    public TextEditorTest(String name) 
    {
        super(name);
    }

    public void testLine()
    {
        TextEditor.Line l = new TextEditor.Line(0,0,20,"this has a = in it", false, 15);
        assertEquals(" in",l.wordToCaret("="));
        assertEquals("this has a = in",l.textToCaret());
        l = new TextEditor.Line(0,0,20,"this doesn't", false, 4);
        assertNull(l.wordToCaret("="));
        l = new TextEditor.Line(0,0,20,"this =doesn't", false, 4);
        assertEquals("doesn't", l.textAfter("="));
        l = new TextEditor.Line(0,0,20,"this =doesn't=either", false, 4);
        assertEquals("doesn't=either", l.textAfter("="));
        l = new TextEditor.Line(0,0,20,"<table>t</table>", false, 8);
        assertEquals("t", l.wordToCaret());


        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 2);
        assertEquals("this", l.wordAtCaret().value);
        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 0);
        assertEquals("this", l.wordAtCaret().value);
        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 1);
        assertEquals("this", l.wordAtCaret().value);
        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 3);
        assertEquals("this", l.wordAtCaret().value);
        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 4);
        assertEquals("this", l.wordAtCaret().value);
        l = new TextEditor.Line(0,0,20,"this is a line of english", false, 5);
        assertEquals("is", l.wordAtCaret().value);

    }

}
