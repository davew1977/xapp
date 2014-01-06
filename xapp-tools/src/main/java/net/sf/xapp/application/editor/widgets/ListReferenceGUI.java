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

import net.sf.xapp.application.api.DummyWidgetContext;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.editor.EditorListener;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.ClassModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ListReferenceGUI extends JFrame
{
    private JPanel m_mainPanel;

    private EditorListener m_editorListener;
    private JPanel m_buttonPanel;
    private JButton m_saveButton;
    private JButton m_closeButton;

    private Box m_mainBox;
    private Box m_midBox;
    private JList m_choiceList;
    private JList m_selectionList;
    private JButton m_addButton;
    private JButton m_removeButton;
    private Node m_node;
    private ReferencePropertyWidget m_referenceProperty;

    public ListReferenceGUI(Node node)
    {
        m_node = node;

        setContentPane(getMainPanel());
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


        if (SwingUtils.DEFAULT_FRAME_ICON != null)
        {
            setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
        }
    }

    public void setGuiListener(EditorListener editorListener)
    {
        m_editorListener = editorListener;
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

    private Box getMainBox()
    {
        if (m_mainBox == null)
        {
            m_mainBox = new Box(BoxLayout.PAGE_AXIS);
            ClassModel cm = m_node.getListNodeContext().getListProperty().getContainedTypeClassModel();
            m_referenceProperty = new ReferencePropertyWidget(true);
            m_referenceProperty.init(new DummyWidgetContext(cm, m_node.getListNodeContext().getListProperty()));
            m_referenceProperty.setValue(null, m_node.getParent().wrappedObject());
            m_mainBox.add(m_referenceProperty.getComponent());

            m_midBox = new Box(BoxLayout.X_AXIS);
            m_choiceList = new JList();
            final JComboBox cb = m_referenceProperty.getComboBox();
            m_choiceList.setModel(cb.getModel());
            m_choiceList.setCellRenderer(cb.getRenderer());
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getActionCommand().equals("comboBoxEdited"))
                    {
                        Object item = cb.getSelectedItem();
                        if (item != null && getSelectionModel().getIndexOf(item) == -1 && m_referenceProperty.getValue() != null)
                        {
                            getSelectionModel().addElement(item);
                            m_selectionList.setSelectedIndex(getSelectionModel().getIndexOf(item));
                        }
                    }
                }
            });

            JScrollPane jsp = new JScrollPane(m_choiceList);
            jsp.setPreferredSize(new Dimension(300, 300));
            m_midBox.add(jsp);
            m_midBox.add(Box.createHorizontalStrut(5));

            Box buttonbox = new Box(BoxLayout.PAGE_AXIS);
            buttonbox.add(getAddButton());
            buttonbox.add(Box.createVerticalStrut(10));
            buttonbox.add(getRemoveButton());

            m_midBox.add(buttonbox);
            m_midBox.add(Box.createHorizontalStrut(5));

            m_selectionList = new JList();
            m_selectionList.setModel(m_referenceProperty.createListModel(m_node.getListNodeContext().getList()));
            m_selectionList.setCellRenderer(cb.getRenderer());

            jsp = new JScrollPane(m_selectionList);
            jsp.setPreferredSize(new Dimension(300, 300));
            m_midBox.add(jsp);

            m_mainBox.add(m_midBox);
        }
        return m_mainBox;
    }

    public List getData()
    {
        ArrayList list = new ArrayList();
        ListModel selModel = m_selectionList.getModel();
        for (int i = 0; i < selModel.getSize(); i++)
        {
            Object key = selModel.getElementAt(i);
            list.add(m_referenceProperty.resolveObj(key));
        }
        return list;
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
                    m_editorListener.save(null, true);
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
                            DefaultComboBoxModel model = getSelectionModel();
                            if (model.getIndexOf(item) == -1)
                            {
                                model.addElement(item);
                                indexes.add(model.getIndexOf(item));
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

    private DefaultComboBoxModel getSelectionModel()
    {
        return (DefaultComboBoxModel) m_selectionList.getModel();
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
}