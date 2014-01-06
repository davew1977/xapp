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
package net.sf.xapp.application.diff;

import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DiffBrowser extends Box implements ActionListener
{
    public final DiffType m_diffType;
    private JEditorPane m_htmlPane;
    JButton m_firstButton;
    JButton m_lastButton;
    JButton m_previousButton;
    JButton m_nextButton;
    private DiffWizardPresenter m_presenter;

    public DiffBrowser(DiffType diffType)
    {
        super(BoxLayout.PAGE_AXIS);
        m_diffType = diffType;
        m_htmlPane = new JEditorPane("text/html", "<html></html>");
        m_htmlPane.setBorder(BorderFactory.createEtchedBorder());
        m_htmlPane.setEditable(false);
        m_htmlPane.setPreferredSize(new Dimension(400,250));
        add(new JScrollPane(m_htmlPane));

        Box buttons = new Box(BoxLayout.LINE_AXIS);
        m_firstButton = createbutton("First", 'F');
        m_previousButton = createbutton("Previous", 'P');
        m_nextButton = createbutton("Next", 'N');
        m_lastButton = createbutton("Last", 'L');
        buttons.add(m_firstButton);
        buttons.add(m_previousButton);
        buttons.add(m_nextButton);
        buttons.add(m_lastButton);
        add(buttons);

    }

    private JButton createbutton(String name, char mnemonic)
    {
        JButton b = new JButton(name);
        b.setMinimumSize(new Dimension(100,20));
        b.setPreferredSize(new Dimension(100,20));
        b.setMaximumSize(new Dimension(100,20));
        b.setMnemonic(new Integer(mnemonic));
        b.addActionListener(this);
        return b;
    }

    public void setContent(String html)
    {
        m_htmlPane.setText(html);
    }

    public static void main(String[] args)
    {
        SwingUtils.showInFrame(new DiffBrowser(DiffType.SIMPLE));
    }

    public void setPresenter(DiffWizardPresenter presenter)
    {
        m_presenter = presenter;
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource().equals(m_previousButton))
        {
            m_presenter.previous();
        }
        else if(e.getSource().equals(m_lastButton))
        {
            m_presenter.last();
        }
        else if(e.getSource().equals(m_nextButton))
        {
            m_presenter.next();
        }
        else if(e.getSource().equals(m_firstButton))
        {
            m_presenter.first();
        }

    }
}
