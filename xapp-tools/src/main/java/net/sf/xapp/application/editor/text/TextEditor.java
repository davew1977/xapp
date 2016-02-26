/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.application.editor.text;

import net.sf.xapp.application.editor.text.undo.UndoManager;
import net.sf.xapp.application.editor.text.undo.Update;
import net.sf.xapp.application.editor.widgets.LiveTemplate;
import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TextEditor extends JTextPane
{
    private UndoManager m_undoManager = new UndoManager();
    private UndoAction m_undoAction = new UndoAction();
    private RedoAction m_redoAction = new RedoAction();
    private boolean m_wordwrap;
    private Map<String, LiveTemplate> m_liveTemplateMap = new HashMap<String, LiveTemplate>();
    private JPopupMenu m_currentPopUp;
    private LiveTemplate m_currentLiveTemplate;
    private static TextEditorListener NULL_LISTENER = new TextEditorListener() {
        @Override
        public void textChanged(String txt) {

        }
    };
    private TextEditorListener listener = NULL_LISTENER;

    public TextEditor()
    {
        setFont(Font.decode(Font.MONOSPACED + "-BOLD-12"));
        setDocument(new DefaultStyledDocument()
        {
            @Override
            public void insertString(int offs, String newText, AttributeSet a) throws BadLocationException
            {
                Line currentLine = getCurrentLine(offs);
                java.util.List<Line> lineOrLinesAfterEdit = currentLine.insert(newText);
                super.insertString(offs, newText, a);
                if (newText != null)
                {
                    m_undoManager.textAdded(offs, newText);
                }
                handleNewText(offs, newText, currentLine, lineOrLinesAfterEdit);
                listener.textChanged(TextEditor.this.getText());
                if (m_currentLiveTemplate != null)
                {
                    boolean stillValid = m_currentLiveTemplate.textInserted(offs, newText);
                    if (!stillValid)
                    {
                        m_currentLiveTemplate = null;
                    }
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException
            {
                String textToRemove = getText(offs, len);
                super.remove(offs, len);
                Line affectedLine = getCurrentLine(offs);
                m_undoManager.textRemoved(offs, textToRemove);
                handleTextRemoved(offs, len, affectedLine, textToRemove);
                listener.textChanged(TextEditor.this.getText());
                if (m_currentLiveTemplate != null)
                {
                    boolean stillValid = m_currentLiveTemplate.textRemoved(offs, len);
                    if (!stillValid)
                    {
                        m_currentLiveTemplate = null;
                    }
                }
            }
        });
        addAction("control D", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Line line = getCurrentLine();
                String extraNewLine = line.m_lastLine ? "\n\n" : "\n";
                String insertString = line.m_text + extraNewLine;
                insert(line.m_startIndex, insertString);
            }
        });
        addAction("control Y", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Line line = getCurrentLine();
                remove(line.m_startIndex, line.m_lastLine ? line.length() : line.length() + 1);
            }
        });
        addAction("TAB", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                Line line = getCurrentLine();
                String word = line.wordToCaret();
                LiveTemplate liveTemplate = m_liveTemplateMap.get(word);
                if (liveTemplate != null)
                {
                    liveTemplate.reset(line.caretIndexInDoc() - word.length());
                    insert(line.caretIndexInDoc(), liveTemplate.getInsertion());
                    remove(line.caretIndexInDoc() - word.length(), word.length());
                    m_currentLiveTemplate = liveTemplate;
                    updateCaretForLiveTemplate();
                }
                else
                {
                    insert(line.caretIndexInDoc(), "\t");
                }
            }
        });
        addAction("ENTER", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (m_currentLiveTemplate != null)
                {
                    updateCaretForLiveTemplate();
                    repaint();
                }
                else
                {
                    insert(getCaretPosition(), "\n");
                }
            }
        });

        addAction("control Z", m_undoAction);
        addAction("control shift Z", m_redoAction);
    }

    public void setListener(TextEditorListener listener) {
        this.listener = listener != null ? listener : NULL_LISTENER;
    }

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (m_currentLiveTemplate != null)
        {
            int[] args = m_currentLiveTemplate.indexRange();
            int startIndex = args[0];
            int len = args[1];
            g.setColor(Color.RED);
            Rectangle b = getBoundsAtIndex(startIndex);
            Rectangle bPrevious = b;
            g.drawLine(b.x, b.y, b.x, b.y + b.height - 1);
            for (int i = 1; i < len + 1; i++)
            {
                Rectangle b2 = getBoundsAtIndex(startIndex + i);
                if (i == len)
                {
                    //close old box
                    g.drawLine(b2.x, b2.y, b2.x, b2.y + b2.height - 1);
                }
                if (b2.y != bPrevious.y)
                {
                    //start new box
                    g.drawLine(bPrevious.x, bPrevious.y, bPrevious.x, bPrevious.y + bPrevious.height - 1);
                    g.drawLine(b2.x, b2.y, b2.x, b2.y + b2.height - 1);
                }
                else
                {
                    g.drawLine(bPrevious.x, bPrevious.y, b2.x, b2.y);
                    g.drawLine(bPrevious.x, bPrevious.y + bPrevious.height - 1, b2.x, b2.y + b2.height - 1);
                }
                bPrevious = b2;
            }
        }
    }

    private void updateCaretForLiveTemplate()
    {
        int newCaret = m_currentLiveTemplate.nextCaretIndex();
        if (!m_currentLiveTemplate.hasMore())
        {
            m_currentLiveTemplate = null;
        }
        setCaretPosition(newCaret);
    }

    public JPopupMenu getCurrentPopUp()
    {
        return m_currentPopUp;
    }

    public void addAction(String keyStroke, Action action)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        getInputMap().put(ks, ks);
        getActionMap().put(ks, action);
    }

    public void removeAction(String keyStroke)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        getInputMap().remove(ks);
        getActionMap().remove(ks);
    }

    public void remove(Word word) {
        remove(word.start, word.length());
    }
    public void remove(int startIndex, int length)
    {
        try
        {
            getDocument().remove(startIndex, length);
        }
        catch (BadLocationException e1)
        {
            throw new RuntimeException(e1);
        }
    }


    public void insert(int index, String insertString)
    {
        try
        {
            getDocument().insertString(index, insertString, null);
        }
        catch (BadLocationException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public StyledDocument getDoc()
    {
        return (StyledDocument) getDocument();
    }

    public abstract void handleNewText(int offs, String newText, Line linePreEdit, java.util.List<Line> lineOrLinesPostEdit);

    public abstract void handleTextRemoved(int offs, int len, Line lineAffected, String removedText);

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return m_wordwrap;
    }

    public void setWordwrap(boolean wordwrap)
    {
        m_wordwrap = wordwrap;

    }

    @Override
    /**
     * Overriden so that text is not converted according to platform. super will add \r chars on windows
     */
    public String getText()
    {
        try
        {
            return getDocument().getText(0, getDocument().getLength());
        }
        catch (BadLocationException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public TreeMap<Integer, Line> getAllLines()
    {
        TreeMap<Integer, Line> lineMap = new TreeMap<Integer, Line>();
        String doc = getText();
        String[] lines = doc.split("\n");
        int lineStart = 0;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            lineMap.put(i, new Line(i, lineStart, lineStart + line.length(), line, i == lines.length - 1, 0));
            lineStart += line.length() + 1;
        }
        return lineMap;
    }

    public Line getCurrentLine()
    {
        int caretIndex = getCaretPosition();
        return getCurrentLine(caretIndex);
    }

    public Line getCurrentLine(int caretIndex)
    {
        String doc = getText();
        String docToCaret = doc.substring(0, caretIndex);
        int lineIndex = docToCaret.split("\n").length;
        int lineStart = docToCaret.lastIndexOf("\n") + 1;
        lineStart = lineStart != -1 ? lineStart : 0;
        int lineEnd = doc.substring(caretIndex).indexOf("\n");
        boolean lastLine = lineEnd == -1;
        if (lastLine)
        {
            lineEnd = doc.length();
        }
        else
        {
            lineEnd += caretIndex;
        }
        String currentLine = doc.substring(lineStart, lineEnd);
        return new Line(lineIndex, lineStart, lineEnd, currentLine, lastLine, caretIndex - lineStart);
    }

    public void addLiveTemplate(String trigger, String template)
    {
        m_liveTemplateMap.put(trigger, new LiveTemplate(template));
    }

    public JPopupMenu newPopUp()
    {
        m_currentPopUp = new JPopupMenu();
        return m_currentPopUp;
    }

    public void addInsertAction(String label, final String insertText)
    {
        addInsertAction(label, insertText, 0);
    }

    public void addInsertAction(String label, final String insertText, final int deleteChars)
    {
        JMenuItem m = new JMenuItem();
        m.setFont(Font.decode("Courier-12"));
        m.setAction(new AbstractAction(label)
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    getDoc().remove(getCaretPosition() - deleteChars, deleteChars);
                    getDoc().insertString(getCaretPosition(), insertText, null);
                }
                catch (BadLocationException e1)
                {
                    throw new RuntimeException();
                }
            }
        });
        m_currentPopUp.add(m);
    }

    public void showPopUp()
    {
        if (m_currentPopUp.getComponentCount() > 0)
        {
            Point pos = getCaretPoint();
            m_currentPopUp.show(this, pos.x, pos.y);
        }
    }

    public Point getCaretPoint()
    {
        Point pos = getCaret().getMagicCaretPosition();
        if (pos == null) pos = new Point(0, 0);
        return pos;
    }


    public Point getPointAtIndex(int i)
    {
        Rectangle boundsAtIndex = getBoundsAtIndex(i);
        return boundsAtIndex != null ? boundsAtIndex.getLocation() : null;
    }

    public Rectangle getBoundsAtIndex(int i)
    {
        try
        {
            return modelToView(i);
        }
        catch (BadLocationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addPopUpAction(AbstractAction action)
    {
        JMenuItem m = new JMenuItem();
        m.setFont(Font.decode("Courier-12"));
        m.setAction(action);
        m_currentPopUp.add(m);
    }

    public void setItalic(int startIndex, int length)
    {
        setItalic(startIndex, length, true);
    }

    public void setItalic(int startIndex, int length, boolean italic)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setItalic(a, italic);
        getDocument().setCharacterAttributes(startIndex, length, a, false);
    }

    public void setUnderline(Word word, boolean underline){
        setUnderline(word.start, word.length(), underline);
    }
    public void setUnderline(int startIndex, int length, boolean underline)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setUnderline(a, underline);
        getDocument().setCharacterAttributes(startIndex, length, a, false);
    }

    public void setBold(int startIndex, int length, boolean bold)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setBold(a, bold);
        getDocument().setCharacterAttributes(startIndex, length, a, false);
    }

    public void setBold(int startIndex, int length)
    {
        setBold(startIndex, length, true);
    }

    public void setForegroundColor(int startIndex, int length, Color color)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, color);
        getDocument().setCharacterAttributes(startIndex, length, a, false);
    }


    protected void setChars(Word word, Boolean bold, Boolean italic, Color color) {
        setChars(word.start, word.length(), bold, italic, color);
    }
    protected void setChars(int startIndex, int length, Boolean bold, Boolean italic, Color color)
    {
        SimpleAttributeSet a = new SimpleAttributeSet();
        if (bold != null)
        {
            StyleConstants.setBold(a, bold);
        }
        if (italic != null)
        {
            StyleConstants.setItalic(a, italic);
        }
        if (color != null)
        {
            StyleConstants.setForeground(a, color);
        }
        getDocument().setCharacterAttributes(startIndex, length, a, true);
    }

    public StyledDocument getDocument()
    {
        return (StyledDocument) super.getDocument();
    }

    public void replaceWordAtCaret(Word word, String newWord)
    {
        //delete whole word at caret
        remove(word);
        //insert new word
        insert(word.start, newWord);
    }


    public void clearLiveTemplate()
    {
        m_liveTemplateMap.clear();
    }

    public static class Line
    {
        public final int m_lineIndex;
        public final int m_startIndex;
        public final int m_endIndex;
        public final String m_text;
        public final boolean m_lastLine;
        public final int m_caretIndexInLine;

        public Line(int lineIndex, int startIndex, int endIndex, String text, boolean lastLine, int caretIndexInLine)
        {
            m_lineIndex = lineIndex;
            m_startIndex = startIndex;
            m_endIndex = endIndex;
            m_text = text;
            m_lastLine = lastLine;
            m_caretIndexInLine = caretIndexInLine;
        }

        public String toString()
        {
            return "Line{" +
                    "m_startIndex=" + m_startIndex +
                    ", m_endIndex=" + m_endIndex +
                    ", m_text='" + m_text + '\'' +
                    ", m_lastLine=" + m_lastLine +
                    ", m_caretIndexInLine=" + m_caretIndexInLine +
                    '}';
        }

        private String insertStr(String str)
        {
            return m_text.substring(0, m_caretIndexInLine) + str + m_text.substring(m_caretIndexInLine);
        }

        public int caretIndexInDoc()
        {
            return m_startIndex + m_caretIndexInLine;
        }

        public java.util.List<Line> insert(String str)
        {
            String text = insertStr(str);
            ArrayList<Line> lineList = new ArrayList<Line>();
            String[] lines = text.split("\n");
            Line lastLine = null;
            for (int i = 0; i < lines.length; i++)
            {
                String line = lines[i];
                boolean isLastLine = m_lastLine && i == lines.length - 1;
                int startIndex = lastLine != null ? lastLine.m_startIndex + lastLine.m_text.length() + 1 : m_startIndex;
                int endIndex = startIndex + line.length();
                Line lineObj = new Line(m_lineIndex + i, startIndex, endIndex, line, isLastLine, -1);
                lastLine = lineObj;
                lineList.add(lineObj);
            }
            return lineList;
        }

        public int length()
        {
            return m_text.length();
        }

        public String wordToCaret() {
            return wordToCaret(Pattern.compile("\\w+"));
        }
        public String wordToCaret(Pattern pattern)
        {
            return wordAtCaret(pattern).wordToCaret();
        }

        /**
         * @param delimeter regexp
         * @return null if delimeter does not exist in text to caret position
         */
        public String wordToCaret(String delimeter)
        {
            String textToCaret = textToCaret();
            String[] chunks = textToCaret.split(delimeter, -1);
            return chunks.length > 1 ? chunks[chunks.length - 1] : null;
        }

        /**
         * returns all text in the line after the delimiter (applied once)
         *
         * @param regexp
         * @return
         */
        public String textAfter(String regexp)
        {
            return textAfter(m_text, regexp);
        }

        public String textToCaret()
        {
            return m_text.substring(0, m_caretIndexInLine);
        }

        public Word wordAtCaret()
        {
            return wordAtCaret("\\w+");
        }

        public Word wordAtCaret(String regex)
        {
            Pattern p = Pattern.compile(regex);
            return wordAtCaret(p);
        }

        public Word wordAtCaret(Pattern p)
        {
            Matcher matcher = p.matcher(m_text);
            while (matcher.find())
            {
                int s = matcher.start();
                int e = matcher.end();
                if (m_caretIndexInLine >= s && m_caretIndexInLine <= e)
                {
                    return new Word(matcher.group(), m_startIndex + s, m_startIndex + e, m_caretIndexInLine - s);
                }
            }
            int c = m_startIndex + m_caretIndexInLine;
            return new Word("", c, c, 0);
        }

        public static String textAfter(String src, String regexp)
        {
            String[] chunks = src.split(regexp, 2);
            return chunks.length > 1 ? chunks[chunks.length - 1] : null;
        }
        public int tabsAtStart()
        {
            int count=0;
            for(int i=0; i< m_text.length(); i++)
            {
                if(m_text.charAt(i)=='\t')
                {
                    count++;
                }
                else
                {
                    break;
                }
            }
            return count;
        }
    }

    public void setTabSize(final int chars)
    {
        Style style = getLogicalStyle();
        TabSet tabs = new TabSet(new TabStop[]{})
        {
            @Override
            public TabStop getTabAfter(float location)
            {
                int gap = getFontMetrics(getFont()).stringWidth("w") * chars;
                int loc = (int) location;
                int next = loc - loc % gap + gap;
                return new TabStop(next);
            }
        };
        StyleConstants.setTabSet(style, tabs);
         
    }

    @Override
    public void setText(String t) {

        m_undoManager.disable();
        m_undoManager.init();
        super.setText(t);
        m_undoManager.enable();
    }

    /**
     * @return the current selected string, or the nearest "word" to the cursor
     */
    public Word selectedWord() {
        String selectedText = getSelectedText();
        if(selectedText==null) {
            return getCurrentLine().wordAtCaret();
        }
        return new Word(selectedText, getSelectionStart(), getSelectionEnd(), 0);
    }


    private class UndoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {

            m_undoManager.disable();
            m_undoManager.flush();
            if (m_undoManager.canUndo())
            {
                Update update = m_undoManager.pullUndo();
                update.undo(TextEditor.this);
            }
            m_undoManager.enable();
        }
    }

    private class RedoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            m_undoManager.disable();
            m_undoManager.flush();
            if (m_undoManager.canRedo())
            {
                Update update = m_undoManager.pullRedo();
                update.redo(TextEditor.this);
            }
            m_undoManager.enable();
        }
    }

    public static void main(String[] args)
    {
        TextEditor t = new TextEditor()
        {
            public void handleNewText(int offs, String newText, Line linePreEdit, List<Line> lineOrLinesPostEdit)
            {

            }

            public void handleTextRemoved(int offs, int len, Line lineAffected, String removedText)
            {

            }
        };
        t.addLiveTemplate("twee", "this $0 will be $1 inserted $2 hello");
        t.addLiveTemplate("b", "a plain template");
        t.addLiveTemplate("x", "this is a live $0 template");
        t.setWordwrap(true);
        JScrollPane jsp = new JScrollPane(t, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setPreferredSize(new Dimension(200, 400));
        SwingUtils.showInFrame(jsp);
    }
}
