/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.api.WidgetContext;
import net.sf.xapp.application.editor.text.TextEditor;
import net.sf.xapp.application.editor.widgets.AbstractPropertyWidget;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;

public class ScriptWidget extends AbstractPropertyWidget<String>
{
    private TextEditor m_textEditor;
    private JScrollPane m_scrollPane;
    private Color DARKGREEN = new Color(0, 128, 0);

    public void init(WidgetContext<String> stringWidgetContext)
    {
        super.init(stringWidgetContext);
        m_textEditor = new TextEditor()
        {
            public void handleNewText(int offs, String newText, Line linePreEdit, List<Line> lineOrLinesPostEdit)
            {
                for (Line line : lineOrLinesPostEdit)
                {
                    if (line.m_text.startsWith("#"))
                    {
                        setBold(line.m_startIndex, line.length());
                        setForegroundColor(line.m_startIndex, line.length(), Color.lightGray);
                    }
                    else
                    {
                        setBold(line.m_startIndex, line.length(), false);
                        setForegroundColor(line.m_startIndex, line.length(), Color.darkGray);
                        Matcher matcher = ScriptPreprocessor.ARG_PATTERN.matcher(line.m_text);
                        while (matcher.find())
                        {
                            setBold(line.m_startIndex + matcher.start(), matcher.group().length());
                            setForegroundColor(line.m_startIndex + matcher.start(), matcher.group().length(), DARKGREEN);
                        }
                    }
                }
            }

            @Override
            public void handleTextRemoved(int offs, int len, Line lineAffected, String removedText)
            {

            }
        };
        m_textEditor.addLiveTemplate("v", "${$END$}");

        m_scrollPane = new JScrollPane(m_textEditor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_scrollPane.setPreferredSize(new Dimension(600, 300));
        m_scrollPane.getViewport().setBackground(Color.white);

    }

    public JComponent getComponent()
    {
        return m_scrollPane;
    }

    public String getValue()
    {
        return m_textEditor.getText();
    }

    public void setValue(String value, ObjectMeta target)
    {
        m_textEditor.setFont(Font.decode("Courier-PLAIN-12"));
        m_textEditor.setText(value);
    }

    public void setEditable(boolean editable)
    {
        m_textEditor.setEditable(editable);
    }

    public TextEditor getTextEditor()
    {
        return m_textEditor;
    }
}