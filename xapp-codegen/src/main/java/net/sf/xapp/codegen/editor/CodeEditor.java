/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.editor;

import net.sf.xapp.application.editor.text.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * currently does not actually have any editor functions!
 */
class CodeEditor extends TextEditor
{
    private Pattern m_delim = Pattern.compile("[\\w]*");
    private Pattern m_allCaps = Pattern.compile("[A-Z][A-Z_0-9]*");
    private Pattern m_keywords = Pattern.compile("abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|const|float|native|super|while");
    private Color m_fieldColor = new Color(102,14,122);
    private Color m_commentColor = new Color(128,128,128);
    private JScrollPane m_scrollPane;

    public void handleNewText(int offs, String newText, Line linePreEdit, List<Line> lineOrLinesPostEdit)
    {
        for (Line line : lineOrLinesPostEdit)
        {
            Matcher matcher = m_delim.matcher(line.m_text);
            setChars(line.m_startIndex, line.length(), false, false, Color.BLACK);
            String trimmedLine = line.m_text.trim();
            if(trimmedLine.startsWith("/**") || trimmedLine.startsWith("*") || trimmedLine.startsWith("*/"))
            {
                setChars(line.m_startIndex, line.length(), false, true, m_commentColor);
            }
            else
            {
                while(matcher.find())
                {
                    String word = matcher.group();
                    int startIndex = line.m_startIndex + matcher.start();
                    int length = matcher.group().length();
                    if(m_keywords.matcher(word).matches())
                    {
                        setChars(startIndex, length, true, false, Color.BLACK);
                    }
                    else if(word.startsWith("m_") || m_allCaps.matcher(word).matches())
                    {
                        setChars(startIndex, length, true, false, m_fieldColor);
                    }
                }
            }
        }
    }

    @Override
    public void handleTextRemoved(int offs, int len, Line lineAffected, String removedText)
    {
        handleNewText(offs, null,null, Arrays.asList(lineAffected));
    }

    public JScrollPane scrollPane()
    {
        if(m_scrollPane==null)
        {
            m_scrollPane = new JScrollPane(this);
            m_scrollPane.getViewport().setBackground(Color.white);
        }
        return m_scrollPane;
    }
}