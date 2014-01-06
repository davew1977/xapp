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
package net.sf.xapp.application.search;

import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchForm extends Box
{
    private JComboBox m_typeCombo;
    private JTextField m_searchStringTF;
    private JTextField m_propertyMatchTF;
    private JCheckBox m_regExpCheckBox;
    private JCheckBox m_searchAllCheckbox;
    private JCheckBox m_matchCaseCheckBox;
    private JList m_resultList;
    private JButton m_searchButton;
    private JButton m_closeButton;
    private final int COLUMN_WIDTH = 200;
    private JFrame m_frame;

    public SearchForm()
    {
        super(BoxLayout.PAGE_AXIS);
        createComponents();
        SwingUtils.setFont(this, SwingUtils.DEFAULT_FONT);
        setBorder(BorderFactory.createEtchedBorder());
        m_searchAllCheckbox.setSelected(true);
        updateViewState();
    }

    public JFrame getFrame()
    {
        if(m_frame==null)
        {
            m_frame = SwingUtils.createFrame(this);
            m_frame.setTitle("Search");
        }
        return m_frame;
    }

    private void createComponents()
    {
        m_typeCombo = createTypeCombo();
        m_regExpCheckBox = new JCheckBox();
        m_searchAllCheckbox = new JCheckBox();
        m_matchCaseCheckBox = new JCheckBox();
        m_searchAllCheckbox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateViewState();
            }
        });
        m_searchStringTF = new JTextField(30);
        m_propertyMatchTF = new JTextField(30);

        //checkbox container

        Box regexpCBBox = wrapCheckbox(m_regExpCheckBox);
        Box searchAllCBBox = wrapCheckbox(m_searchAllCheckbox);
        Box matchCaseCBBox = wrapCheckbox(m_matchCaseCheckBox);

        add(createVerticalStrut(10));
        add(createRow("Search All:", searchAllCBBox));
        add(createRow("Types:", m_typeCombo));
        add(createRow("Regexp: ", regexpCBBox));
        add(createRow("Match Case: ", matchCaseCBBox));
        add(createRow("Search Text: ", m_searchStringTF));
        add(createRow("Property match: ", m_propertyMatchTF));
        add(createVerticalStrut(10));

        //buttons
        m_searchButton = createButton("Search");
        m_closeButton = createButton("Close");
        Box buttonRow = createHorizontalBox();
        buttonRow.add(m_searchButton);
        buttonRow.add(createHorizontalStrut(10));
        buttonRow.add(m_closeButton);
        add(buttonRow);

        add(createVerticalStrut(10));
        add(createResultBox());

    }

    private void updateViewState()
    {
        m_typeCombo.setEnabled(!m_searchAllCheckbox.isSelected());
    }

    private Box wrapCheckbox(JCheckBox cb)
    {
        Box cbbox = createHorizontalBox();
        cbbox.add(cb);
        cbbox.add(createHorizontalGlue());
        return cbbox;
    }

    private JButton createButton(String label)
    {
        JButton b = new JButton(label);
        //b.setPreferredSize(new Dimension(70,20));
        return b;
    }

    private Box createRow(String labelText, JComponent comp)
    {
        Box row1 = new Box(BoxLayout.LINE_AXIS);
        comp.setPreferredSize(new Dimension(COLUMN_WIDTH, comp.getPreferredSize().height));
        JLabel label = createLabel(labelText);
        row1.add(createHorizontalStrut(10));
        row1.add(label);
        row1.add(comp);
        row1.add(createRigidArea(new Dimension(4,4)));
        return row1;
    }

    private JLabel createLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(COLUMN_WIDTH, label.getPreferredSize().height));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private Box createResultBox()
    {
        Box box = new Box(BoxLayout.LINE_AXIS);
        box.setBorder(BorderFactory.createTitledBorder("Results:"));

        m_resultList = new JList();
        JScrollPane jsp = new JScrollPane(m_resultList);
        jsp.setPreferredSize(new Dimension(500,400));
        box.add(jsp);
        return box;
    }

    private JComboBox createTypeCombo()
    {
        return new JComboBox(new Object[]{"A", "B", "C"});
    }

    public JComboBox getTypeCombo()
    {
        return m_typeCombo;
    }

    public JButton getCloseButton()
    {
        return m_closeButton;
    }

    public JButton getSearchButton()
    {
        return m_searchButton;
    }

    public JCheckBox getRegExpCheckBox()
    {
        return m_regExpCheckBox;
    }

    public JTextField getSearchStringTF()
    {
        return m_searchStringTF;
    }

    public JCheckBox getSearchAllCheckbox()
    {
        return m_searchAllCheckbox;
    }

    public JTextField getPropertyMatchTF()
    {
        return m_propertyMatchTF;
    }

    public JList getResultList()
    {
        return m_resultList;
    }

    public static void main(String[] args)
    {
        new SearchForm().getFrame().setVisible(true);
    }

    public void clear()
    {
        m_resultList.setModel(null);
        m_searchStringTF.setText(null);
        m_typeCombo.setSelectedItem(null);
    }

    public JCheckBox getMatchCaseCheckBox()
    {
        return m_matchCaseCheckBox;
    }
}
