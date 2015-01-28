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
package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.awt.*;

public class FreeTextPropertyWidget extends AbstractPropertyWidget
{
    private JScrollPane m_scrollPane;
    private JTextArea m_textArea;
    private Font font;

    public void setEditable(boolean editable)
    {
        m_textArea.setEditable(editable);
    }

    public JComponent getComponent()
    {
        if(m_scrollPane==null)
        {
            m_scrollPane = new JScrollPane(getTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            m_scrollPane.setPreferredSize(new Dimension(160,160));
            String args = m_widgetContext.getArgs();
            if(args !=null && !args.equals(""))
            {
                String[] s = args.split(",");
                int w = Integer.parseInt(s[0]);
                int h = Integer.parseInt(s[1]);
                m_scrollPane.setPreferredSize(new Dimension(w,h));
                if (s.length == 3)
                {
                    font = Font.decode(s[2]);
                }
            }
            m_scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                  Short.MAX_VALUE));
        }
        return m_scrollPane;
    }

    protected JTextArea getTextArea()
    {
        if(m_textArea==null)
        {
            m_textArea = new JTextArea();
            m_textArea.setWrapStyleWord(true);
            m_textArea.setLineWrap(true);

        }
        return m_textArea;
    }

    public Object getValue()
    {
        return m_textArea.getText().equals("")? null : m_textArea.getText();
    }

    public void setValue(Object value, ObjectMeta target)
    {
        if(font != null) {
            m_textArea.setFont(font);
        }
        getTextArea().setText((String) value);
    }
}
