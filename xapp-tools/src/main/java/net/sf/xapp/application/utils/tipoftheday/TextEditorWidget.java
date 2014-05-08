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
package net.sf.xapp.application.utils.tipoftheday;

import net.sf.xapp.application.api.WidgetContext;
import net.sf.xapp.application.editor.widgets.AbstractPropertyWidget;
import net.sf.xapp.application.editor.text.TextEditor;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditorWidget extends AbstractPropertyWidget
{
    private JScrollPane m_scrollPane;
    private TextEditor m_textEditor;
    private static final Color DARK_BLUE = new Color(0,0,180);

    public String validate()
    {
        String[] lines = m_textEditor.getText().split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            //TODO
        }
        return null;
    }

    @Override
    public void init(WidgetContext context)
    {
        super.init(context);
    }

    public void setEditable(boolean editable)
    {
        m_textEditor.setEditable(editable);
    }

    public JComponent getComponent()
    {
        if (m_scrollPane == null)
        {
            m_scrollPane = new JScrollPane(getTextEditor(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            m_scrollPane.setPreferredSize(new Dimension(300,400));
        }
        return m_scrollPane;
    }

    public TextEditor getTextEditor()
    {
        if (m_textEditor == null)
        {
            m_textEditor = new TextEditor()
            {
                Pattern pattern = Pattern.compile("<[\\w\\W&&[^>]]*>");
                public void handleNewText(int offs, String newText, Line linePreEdit, List<Line> lineOrLinesPostEdit)
                {
                    for (Line line : lineOrLinesPostEdit)
                    {
                        Matcher matcher = pattern.matcher(line.m_text);
                        int lastEnd = line.m_startIndex;
                        int lastEndInLine = 0;
                        SimpleAttributeSet as = new SimpleAttributeSet();
                        while(matcher.find())
                        {
                            //set start chunk black
                            StyleConstants.setForeground(as, Color.BLACK);
                            StyleConstants.setBold(as, false);
                            m_textEditor.getDoc().setCharacterAttributes(lastEnd, matcher.start() - lastEndInLine, as, true);
                            //set tag blue
                            as = new SimpleAttributeSet();
                            StyleConstants.setForeground(as, DARK_BLUE);
                            StyleConstants.setBold(as, true);
                            int length = matcher.end() - matcher.start();
                            m_textEditor.getDoc().setCharacterAttributes(line.m_startIndex + matcher.start(), length, as, true);
                            lastEnd = line.m_startIndex + matcher.end();
                            lastEndInLine = matcher.end();
                        }
                        //set the trailing bit black
                        as = new SimpleAttributeSet();
                        StyleConstants.setForeground(as, Color.BLACK);
                        m_textEditor.getDoc().setCharacterAttributes(lastEnd, line.length() - lastEndInLine, as, true);


                    }
                }

                public void handleTextRemoved(int offs, int len, Line lineAffected, String removedText)
                {
                    handleNewText(-1, null, null, Arrays.asList(lineAffected));
                }
            };
            m_textEditor.setWordwrap(true);
            m_textEditor.addLiveTemplate("a", "<a href=\"$END$\"></a>");
            m_textEditor.addLiveTemplate("b", "<b>$END$</b>");
            m_textEditor.addLiveTemplate("i", "<i>$END$</i>");
            m_textEditor.addLiveTemplate("img", "<img src=\"$END$\"/>");
            m_textEditor.addLiveTemplate("table", "<table>\n$END$\n</table>");
            m_textEditor.addLiveTemplate("tr", "<tr>$END$</tr>");
            m_textEditor.addLiveTemplate("td", "<td>$END$</td>");
            m_textEditor.addLiveTemplate("p", "<p>$END$</p>");
        }
        return m_textEditor;
    }


    public Object getValue()
    {
        return m_textEditor.getText();
    }

    public void setValue(Object value, ObjectMeta target)
    {
        getTextEditor().setText((String) value);
    }

}