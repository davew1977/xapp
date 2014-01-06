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

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.html.BrowserView;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TipOfDayDialog extends JDialog
{
    private BrowserView m_viewPanel;
    private JButton m_next;
    private JButton m_previous;
    private JButton m_close;
    private java.util.List<Tip> m_tips;
    private int m_tipIndex = 0;

    public TipOfDayDialog(Frame owner, java.util.List<Tip> tips)
    {
        super(owner, true);
        m_tips = tips;
        m_tipIndex = (int) (Math.random() * tips.size());

        JPanel mainPanel = new JPanel(new BorderLayout());
        m_viewPanel = new BrowserView();
        m_viewPanel.setPreferredSize(new Dimension(400,300));
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setPreferredSize(new Dimension(400,30));
        buttonBox.add(Box.createHorizontalStrut(80));
        buttonBox.add(getPreviousButton());
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(getNextButton());
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(getCloseButton());
        mainPanel.add(m_viewPanel, "Center");
        Border border = BorderFactory.createMatteBorder(10, 15, 10, 10, Color.LIGHT_GRAY);
        m_viewPanel.setBorder(border);
        m_viewPanel.setBackground(Color.WHITE);
        m_viewPanel.setOpaque(true);
        mainPanel.add(buttonBox, "South");
        setContentPane(mainPanel);
        updateTip();
        SwingUtils.setFont(this, SwingUtils.DEFAULT_FONT);
        setTitle("Tip of the Day");
        pack();
    }

    private void updateTip()
    {
        Tip tip = m_tips.get(m_tipIndex);
        m_viewPanel.setHTML("<html>" + tip.getText() + "</html>");
    }


    public JButton getPreviousButton()
    {
        if(m_previous==null)
        {
            m_previous = new JButton(new AbstractAction("Previous")
            {
                public void actionPerformed(ActionEvent e)
                {
                    m_tipIndex = m_tipIndex==0 ? m_tips.size()-1 : m_tipIndex-1;
                    updateTip();
                }
            });
        }
        return m_previous;
    }

    public JButton getNextButton()
    {
        if(m_next==null)
        {
            m_next = new JButton(new AbstractAction("next")
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println(m_tipIndex);
                    m_tipIndex = ++m_tipIndex % m_tips.size();
                    updateTip();
                }
            });
        }
        return m_next;
    }

    public JButton getCloseButton()
    {
        if(m_close==null)
        {
            m_close = new JButton(new AbstractAction("close")
            {
                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                }
            });
        }
        return m_close;
    }
}
