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

import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI that allows the user to select one or more items from a list
 */
public class SelectionGUI extends JFrame
{
    private JPanel m_mainPanel;

    private SelectionGUIListener m_listener;
    private JPanel m_buttonPanel;
    private JButton m_saveButton;
    private JButton m_closeButton;

    private Box m_mainBox;
    /**
     * list displaying the items to choose from
     */
    private JList m_choiceList;
    /**
     * list displaying the current selection
     */
    private JList m_selectionList;
    private JButton m_addButton;
    private JButton m_removeButton;
    private JComboBox m_searchCombo;
    private List<?> m_items;
    private List<?> m_currentSelection;

    public void init(List<?> items, List<?> currentSelection)
    {
        m_items = items;
        m_currentSelection = currentSelection;

        setContentPane(getMainPanel());
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        if (SwingUtils.DEFAULT_FRAME_ICON != null)
        {
            setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
        }
    }

    public void setListener(SelectionGUIListener listener)
    {
        m_listener = listener;
    }

    private Container getMainPanel()
    {
        if (m_mainPanel == null)
        {
            m_mainPanel = new JPanel(new BorderLayout());
            m_mainPanel.add(getMainBox(), "North");
            m_mainPanel.add(getButtonPanel(), "South");
            SwingUtils.setFont(m_mainPanel, SwingUtils.DEFAULT_FONT);
        }
        return m_mainPanel;
    }

    public void setSearchCombo(JComboBox combo)
    {
        m_searchCombo = combo;
    }

    private JComboBox getSearchCombo()
    {
        if (m_searchCombo == null)
        {
            m_searchCombo = new JComboBox();
            m_searchCombo.setModel(new DefaultComboBoxModel(m_items.toArray()));
            m_searchCombo.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        Object item = m_searchCombo.getSelectedItem();
                        if (item != null && getSelectionModel().indexOf(item) == -1)
                        {
                            getSelectionModel().addElement(item);
                            m_selectionList.setSelectedIndex(getSelectionModel().indexOf(item));
                        }
                    }
                }
            });
        }
        return m_searchCombo;
    }

    private Box getMainBox()
    {
        if (m_mainBox == null)
        {
            m_mainBox = new Box(BoxLayout.PAGE_AXIS);
            m_mainBox.add(getSearchCombo());

            Box midBox = new Box(BoxLayout.X_AXIS);

            JScrollPane jsp = new JScrollPane(getChoiceList());
            jsp.setPreferredSize(new Dimension(300, 300));
            midBox.add(jsp);
            midBox.add(Box.createHorizontalStrut(5));

            Box buttonbox = new Box(BoxLayout.PAGE_AXIS);
            buttonbox.add(getAddButton());
            buttonbox.add(Box.createVerticalStrut(10));
            buttonbox.add(getRemoveButton());

            midBox.add(buttonbox);
            midBox.add(Box.createHorizontalStrut(5));


            jsp = new JScrollPane(getSelectionList());
            jsp.setPreferredSize(new Dimension(300, 300));
            midBox.add(jsp);

            m_mainBox.add(midBox);
        }
        return m_mainBox;
    }

    public JList getSelectionList()
    {
        if(m_selectionList==null)
        {
            m_selectionList = new JList();
            m_selectionList.setModel(createListModel(m_currentSelection));
            m_selectionList.setCellRenderer(getSearchCombo().getRenderer());
        }
        return m_selectionList;
    }

    public JList getChoiceList()
    {
        if(m_choiceList==null)
        {
            m_choiceList = new JList();
            m_choiceList.setModel(createListModel(m_items));
            m_choiceList.setCellRenderer(getSearchCombo().getRenderer());
        }
        return m_choiceList;
    }

    private ListModel createListModel(List<?> items)
    {
        DefaultListModel m = new DefaultListModel();
        for (Object item : items)
        {
            m.addElement(item);
        }
        return m;
    }

    private JPanel getButtonPanel()
    {
        if (m_buttonPanel == null)
        {
            m_buttonPanel = new JPanel();
            m_buttonPanel.add(getSaveButton());
            m_buttonPanel.add(getCloseButton());
        }
        return m_buttonPanel;
    }

    private JButton getCloseButton()
    {
        if (m_closeButton == null)
        {
            m_closeButton = new JButton();
            AbstractAction action = new AbstractAction("Close")
            {
                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                }
            };
            action.putValue(Action.MNEMONIC_KEY, new Integer('C'));
            m_closeButton.setAction(action);

        }
        return m_closeButton;
    }

    private JButton getSaveButton()
    {
        if (m_saveButton == null)
        {
            m_saveButton = new JButton();
            AbstractAction action = new AbstractAction("Save")
            {
                public void actionPerformed(ActionEvent e)
                {
                    DefaultListModel listModel = (DefaultListModel) m_selectionList.getModel();
                    m_listener.selectionMade(Arrays.asList(listModel.toArray()));
                    setVisible(false);
                }
            };
            action.putValue(Action.MNEMONIC_KEY, new Integer('S'));
            m_saveButton.setAction(action);
        }
        return m_saveButton;
    }

    private JButton getAddButton()
    {
        if (m_addButton == null)
        {
            m_addButton = new JButton();
            AbstractAction action = new AbstractAction("Add")
            {
                public void actionPerformed(ActionEvent e)
                {
                    Object[] items = m_choiceList.getSelectedValues();
                    List<Integer> indexes = new ArrayList<Integer>();
                    if (items != null)
                    {
                        for (Object item : items)
                        {
                            DefaultListModel model = getSelectionModel();
                            if (model.indexOf(item) == -1)
                            {
                                model.addElement(item);
                                indexes.add(model.indexOf(item));
                            }
                        }
                    }
                    int[] ints = new int[indexes.size()];
                    for (int i = 0; i < indexes.size(); i++)
                    {
                        Integer integer = indexes.get(i);
                        ints[i] = integer;
                    }
                    m_selectionList.setSelectedIndices(ints);
                }
            };
            action.putValue(Action.MNEMONIC_KEY, new Integer('A'));
            m_addButton.setAction(action);
            m_addButton.setPreferredSize(new Dimension(100, 20));
        }
        return m_addButton;
    }

    private DefaultListModel getSelectionModel()
    {
        return (DefaultListModel) m_selectionList.getModel();
    }

    private JButton getRemoveButton()
    {
        if (m_removeButton == null)
        {
            m_removeButton = new JButton();
            AbstractAction action = new AbstractAction("Remove")
            {
                public void actionPerformed(ActionEvent e)
                {
                    Object[] items = m_selectionList.getSelectedValues();
                    if (items != null)
                    {
                        for (Object item : items)
                        {
                            getSelectionModel().removeElement(item);
                        }
                    }
                    m_selectionList.setSelectedIndex(0);
                }
            };
            action.putValue(Action.MNEMONIC_KEY, new Integer('R'));
            m_removeButton.setAction(action);
            m_removeButton.setPreferredSize(new Dimension(100, 20));
        }
        return m_removeButton;
    }

    public static void main(String[] args)
    {
        List<String> items = Arrays.asList("David", "John", "Jackie", "Phil");
        List<String> selection = Arrays.asList("Davi", "John", "Jackie", "Phil");
        SelectionGUI s = new SelectionGUI();
        s.init(items, selection);
        s.setListener(new SelectionGUIListener()
        {
            public void selectionMade(List<?> items)
            {
                System.out.println(items);
            }
        });
        s.setVisible(true);
    }
}