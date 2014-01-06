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

public class DiffSummary extends Box implements ActionListener
{
    private JEditorPane m_htmlPane;
    private JButton m_browseConflictsButton;
    private JButton m_browseAllButton;
    private JButton m_acceptAllButton;
    private DiffWizardPresenter m_presenter;
    private JButton m_acceptNonConflictsButton;

    public DiffSummary()
    {
        super(BoxLayout.PAGE_AXIS);
        m_htmlPane = new JEditorPane("text/html", "<html></html>");
        m_htmlPane.setBorder(BorderFactory.createEtchedBorder());
        m_htmlPane.setEditable(false);
        m_htmlPane.setPreferredSize(new Dimension(400,250));
        add(m_htmlPane);

        m_browseAllButton = createbutton("Browse All", 'B');
        m_browseConflictsButton = createbutton("Browse Conflicts", 'C');
        m_acceptAllButton= createbutton("Accept All", 'A');
        m_acceptNonConflictsButton= createbutton("Accept Non Conflicts", 'N');

        Box buttonLeft = new Box(BoxLayout.PAGE_AXIS);
        Box buttonRight = new Box(BoxLayout.PAGE_AXIS);
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);
        buttonLeft.add(m_acceptNonConflictsButton);
        buttonLeft.add(Box.createVerticalGlue());
        buttonLeft.add(m_acceptAllButton);
        buttonRight.add(m_browseAllButton);
        buttonRight.add(Box.createVerticalGlue());
        buttonRight.add(m_browseConflictsButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(buttonLeft);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(buttonRight);
        buttonBox.add(Box.createHorizontalGlue());
        add(buttonBox);

        SwingUtils.setFont(this, SwingUtils.DEFAULT_FONT);
    }

    private JButton createbutton(String name, char mnemonic)
    {
        JButton b = new JButton(name);
        b.setMinimumSize(new Dimension(200,20));
        b.setPreferredSize(new Dimension(200,20));
        b.setMaximumSize(new Dimension(200,20));
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
        SwingUtils.showInFrame(new DiffSummary());
    }

    public void setPresenter(DiffWizardPresenter presenter)
    {
        m_presenter = presenter;
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource().equals(m_acceptAllButton))
        {
            m_presenter.acceptAll();
        }
        else if(e.getSource().equals(m_acceptNonConflictsButton))
        {
            m_presenter.acceptNonConflicts();
        }
        else if(e.getSource().equals(m_browseAllButton))
        {
            m_presenter.browseAll();
        }
        else if(e.getSource().equals(m_browseConflictsButton))
        {
            m_presenter.browseConflicts();
        }

    }
}
