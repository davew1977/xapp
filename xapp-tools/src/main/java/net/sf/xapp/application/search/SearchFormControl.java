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

import net.sf.xapp.application.api.Command;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.html.HTML;
import net.sf.xapp.application.utils.html.HTMLImpl;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.difftracking.ChangeModel;
import net.sf.xapp.objectmodelling.difftracking.ChangeSet;
import net.sf.xapp.tree.Tree;
import net.sf.xapp.tree.TreeNode;
import net.sf.xapp.utils.ClassUtils;
import net.sf.xapp.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class SearchFormControl
{
    private SearchForm m_searchForm;
    private ClassDatabase m_classDatabase;
    private SearchContext m_searchContext;
    private Object m_searchObject;
    private List<Command> m_currentKeyCommands = new ArrayList<Command>();

    public SearchFormControl(ClassDatabase classDatabase, SearchContext searchContext)
    {
        m_classDatabase = classDatabase;
        m_searchContext = searchContext;
        m_searchForm = new SearchForm();

        wireButtons();

        List<ClassModel> classModelList = classDatabase.enumerateClassModels();
        classModelList.remove(classDatabase.getClassModel(ChangeSet.class));
        classModelList.remove(classDatabase.getClassModel(ChangeModel.class));
        classModelList.remove(classDatabase.getClassModel(TreeNode.class));
        classModelList.remove(classDatabase.getClassModel(Tree.class));
        Collections.sort(classModelList, new ClassModelComparator());
        m_searchForm.getTypeCombo().setRenderer(new DefaultListCellRenderer()
        {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                value = ClassUtils.toHeirarchyString((ClassModel) value);

                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        m_searchForm.getTypeCombo().setModel(new DefaultComboBoxModel(new Vector<Object>(classModelList)));

        m_searchForm.getResultList().setCellRenderer(new DefaultListCellRenderer()
        {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                Object obj = value;
                ClassModel classModel = m_classDatabase.getClassModel(value.getClass());
                HTML html = new HTMLImpl();
                html.table();
                html.tr().td(ClassUtils.toHeirarchyString(classModel), 200);
                html.color(Color.BLUE).td(toStringSpecial(value, classModel));
                value = html.htmlDoc();
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Icon icon = m_searchContext.getIcon(obj);
                setIcon(icon);
                return this;
            }
        });

        m_searchForm.getResultList().addListSelectionListener(new KeyCommandListSelectionListener());
        m_searchForm.getResultList().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && m_searchContext != null)
                {
                    m_searchContext.searchResultSelected(m_searchForm.getResultList().getSelectedValue());
                }

            }
        });

        m_searchForm.getResultList().addMouseListener(new SearchMouseListener());
    }

    private Object toStringSpecial(Object value)
    {
        return toStringSpecial(value, m_classDatabase.getClassModel(value.getClass()));
    }

    private String toStringSpecial(Object value, ClassModel classModel)
    {
        if (classModel.hasKey())
        {
            value = classModel.getKey(value);
        }
        else
        {
            value = value.toString();
        }
        return String.valueOf(value);
    }

    public void setSearchObject(Object searchObject)
    {
        m_searchForm.getFrame().setTitle("Searching: " + toStringSpecial(searchObject));
        m_searchObject = searchObject;
    }

    public void showFrame()
    {
        m_searchForm.getFrame().setVisible(true);
    }

    private void wireButtons()
    {
        m_searchForm.getSearchButton().setAction(new SearchAction());
        m_searchForm.getCloseButton().setAction(new CloseAction());
    }

    public static void main(String[] args)
    {
    }

    private class ClassModelComparator implements Comparator<ClassModel>
    {
        public int compare(ClassModel cm1, ClassModel cm2)
        {
            return ClassUtils.toHeirarchyString(cm1).compareTo(ClassUtils.toHeirarchyString(cm2));
        }
    }

    private class ObjectComparator implements Comparator<Object>
    {
        ClassModelComparator cmc = new ClassModelComparator();

        public int compare(Object obj1, Object obj2)
        {
            ClassModel cm1 = m_classDatabase.getClassModel(obj1.getClass());
            ClassModel cm2 = m_classDatabase.getClassModel(obj2.getClass());
            String objStr1 = ClassUtils.toHeirarchyString(cm1) + " : " + SearchFormControl.this.toStringSpecial(obj1, cm1);
            String objStr2 = ClassUtils.toHeirarchyString(cm2) + " : " + SearchFormControl.this.toStringSpecial(obj2, cm2);
            return objStr1.compareTo(objStr2);
        }
    }

    private class SearchAction extends AbstractAction
    {
        private SearchAction()
        {
            super("Search");
            putValue(MNEMONIC_KEY, new Integer('S'));
        }

        public void actionPerformed(ActionEvent e)
        {
            ClassModel cm = m_classDatabase.getClassModel(m_searchObject.getClass());
            ClassModel selectedClassModel = (ClassModel) m_searchForm.getTypeCombo().getSelectedItem();
            String searchText = m_searchForm.getSearchStringTF().getText();
            String propertyMatch = m_searchForm.getPropertyMatchTF().getText();
            Map<String, String> propMatchMap = StringUtils.parsePropertyString(propertyMatch, ";");

            if ("".equals(searchText)) searchText = null;
            boolean isRegExpr = m_searchForm.getRegExpCheckBox().isSelected();
            boolean matchCase = m_searchForm.getMatchCaseCheckBox().isSelected();
            Class searchType = m_searchForm.getSearchAllCheckbox().isSelected() ? null : selectedClassModel.getContainedClass();

            //System.out.println("search " + m_searchObject + " " + searchType + " " + isRegExpr + " " + searchText + " " + matchCase);

            Set<Object> results = cm.search(m_searchObject, searchType, isRegExpr, searchText, matchCase, propMatchMap);

            Vector<Object> resultVect = new Vector<Object>(results);
            Collections.sort(resultVect, new ObjectComparator());
            m_searchForm.getResultList().setModel(new DefaultComboBoxModel(resultVect));
        }
    }

    private class CloseAction extends AbstractAction
    {
        private CloseAction()
        {
            super("Close");
            putValue(MNEMONIC_KEY, new Integer('C'));
        }

        public void actionPerformed(ActionEvent e)
        {
            m_searchForm.getFrame().setVisible(false);
        }
    }

    private class SearchMouseListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                Object[] selectedObjects = m_searchForm.getResultList().getSelectedValues();

                List<Object> selectedObjectList = Arrays.asList(selectedObjects);
                List<Node> nodes = m_searchContext.getNodes(selectedObjectList);
                if (!selectedObjectList.isEmpty())
                {
                    Object arg = nodes.size() == 1 ? nodes.get(0) : nodes;
                    List<Command> commands = m_searchContext.getCommands(nodes);
                    JPopupMenu popup = createPopUp(commands, arg);
                    popup.show(e.getComponent(), e.getX() + 10, e.getY() + 10);
                }
            }
        }

        private JPopupMenu createPopUp(List<Command> commands, final Object arg)
        {
            JPopupMenu menu = new JPopupMenu();
            for (final Command command : commands)
            {
                JMenuItem menuItem = new JMenuItem(command.getName());
                menuItem.setFont(SwingUtils.DEFAULT_FONT);
                menuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        command.execute(arg);
                        m_searchContext.update();
                        m_searchForm.repaint();
                    }
                });
                menuItem.setToolTipText(command.getDescription());
                menuItem.setAccelerator(KeyStroke.getKeyStroke(command.getKeyStroke()));
                menu.add(menuItem);
            }
            return menu;
        }
    }

    private class KeyCommandListSelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if (e.getValueIsAdjusting()) return;
            //remove current key commands
            for (Command command : m_currentKeyCommands)
            {
                removeKeyStroke(command);
            }
            m_currentKeyCommands.clear();
            //is it single/multi selection?
            Object[] selectedObjects = m_searchForm.getResultList().getSelectedValues();

            List<Object> selectedObjectList = Arrays.asList(selectedObjects);
            List<Node> nodes = m_searchContext.getNodes(selectedObjectList);
            Object arg = nodes.size() == 1 ? nodes.get(0) : nodes;
            m_currentKeyCommands = m_searchContext.getCommands(nodes);

            for (Command command : m_currentKeyCommands)
            {
                addKeyStroke(arg, command);
            }
        }

        private void addKeyStroke(final Object arg, final Command command)
        {
            InputMap im = m_searchForm.getResultList().getInputMap();
            ActionMap actionMap = m_searchForm.getResultList().getActionMap();
            String ks = command.getKeyStroke();
            KeyStroke k = KeyStroke.getKeyStroke(ks);
            if (k != null)
            {
                im.put(k, k);
                actionMap.put(k, new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        command.execute(arg);
                        m_searchContext.update();
                        m_searchForm.repaint();
                    }
                });
            }
            else if (ks != null)
            {
                System.out.println("WARNING - no key stroke matched for " + ks);
            }
        }

        private void removeKeyStroke(Command command)
        {
            InputMap im = m_searchForm.getResultList().getInputMap();
            ActionMap actionMap = m_searchForm.getResultList().getActionMap();
            if (command.getKeyStroke() == null) return;
            KeyStroke ks = KeyStroke.getKeyStroke(command.getKeyStroke());
            assert ks != null;
            actionMap.remove(ks);
            im.remove(ks);
        }
    }
}
