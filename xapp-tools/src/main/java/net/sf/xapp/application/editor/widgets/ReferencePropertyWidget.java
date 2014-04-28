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

import net.sf.xapp.application.api.WidgetContext;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.tree.Tree;
import net.sf.xapp.tree.TreeNode;
import net.sf.xapp.utils.XappException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static net.sf.xapp.utils.StringUtils.*;

public class ReferencePropertyWidget<T> extends AbstractPropertyWidget<T>
{
    protected JComboBox m_comboBox;
    protected Object m_target;
    protected Tree m_root; //has a value if contained type is tree

    public final String NULL = "null";
    private Object m_parentObject;

    private final boolean containedByListReferenceGUI;


    public ReferencePropertyWidget(boolean containedByListReferenceGUI)
    {
        this.containedByListReferenceGUI = containedByListReferenceGUI;
        //get Options
        m_comboBox = new JComboBox();
        m_comboBox.setEditable(false);
        m_comboBox.setPreferredSize(new Dimension(160, 20));
        m_comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        KeyStroke controlT = KeyStroke.getKeyStroke("control SPACE");
        getTextField().getInputMap().put(controlT, controlT);
        getTextField().getActionMap().put(controlT, new CodeCompleteAction());
        ToolTipManager.sharedInstance().registerComponent(m_comboBox);
    }

    public String validate()
    {
        Object item = m_comboBox.getSelectedItem();
        if (item == null || item.equals(NULL))
        {
            return null;
        }
        Object obj = getClassModel().getInstanceNoCheck(item.toString());
        if (obj == null)
        {
            return getClassModel() + " with key " + item + " does not exist";
        }
        return null;
    }

    public void setEditable(boolean editable)
    {
        m_comboBox.setEditable(editable);
        m_comboBox.setEnabled(editable);
    }

    public JComponent getComponent()
    {
        return m_comboBox;
    }

    public T getValue()
    {
        Object item = m_comboBox.getSelectedItem();
        return resolveObj(item);
    }

    public T resolveObj(Object key)
    {
        if (key == null || key.equals(NULL))
        {
            return null;
        }
        return getClassModel().getInstanceNoCheck(key.toString());
    }

    private ClassModel<T> getClassModel()
    {
        return m_widgetContext.getPropertyClassModel();
    }

    public void setValue(T value, Object target)
    {
        init(m_widgetContext);
        m_parentObject = target;
        m_comboBox.setSelectedItem(getDisplayKey(value));
    }

    private String getDisplayKey(T value)
    {
        if(value==null)
        {
            return null;
        }
        return getClassModel().getPrimaryKey(value);
    }

    public void init(WidgetContext<T> context)
    {
        super.init(context);
        Vector list = getClassModel().getAllInstancesInHierarchy(getQuery());
        setComboValues(list);
        m_comboBox.setRenderer(new DefaultListCellRenderer()
        {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                T obj = m_widgetContext.getPropertyClassModel().getInstanceNoCheck(value);
                setToolTipText(getTooltip(obj));
                String t = getLabelText(obj);
                value = t != null ? t : value;
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        if (getClassModel().isTreeType())
        {
            m_root = new Tree();
            Tree root = getClassModel().getTreeRoot();
            m_root.setPathSeparator(root.pathSeparator());
            m_root.getChildren().add(root);
            m_comboBox.setEditable(true);
        }
        else
        {
            m_comboBox.setEditable(false);
        }
    }

    private void setComboValues(List list)
    {
        DefaultComboBoxModel model = createListModel(list);
        m_comboBox.setModel(model);
    }

    protected String getLabelText(T obj)
    {
        return null;
    }

    /**
     * Allows subclasses to override and provide their own tool tips
     *
     * @param value
     * @return
     */
    protected String getTooltip(T value)
    {
        String tooltipMethod = m_widgetContext.tooltipMethod();
        if(tooltipMethod !=null && !tooltipMethod.equals("") && value!=null)
        {
            return (String) ClassModel.tryAndInvoke(value, tooltipMethod);
        }
        else
        {
        return String.valueOf(value);
    }
    }

    public DefaultComboBoxModel createListModel(List list)
    {
        //convert to a list of strings
        Vector<String> stringList = new Vector<String>();
        boolean sorted = preSort(list);
        for (Object o : list)
        {
            String fullKey = getClassModel().getPrimaryKey((T) o);
            stringList.add(fullKey);
        }

        if (!sorted)
        {
            Collections.sort(stringList);
        }

        if (!(containedByListReferenceGUI || isMandatory()))
        {
            stringList.add(0, NULL);
        }
        return new DefaultComboBoxModel(stringList);
    }

    /**
     * Should be overriden to provide a custom sorting on list items
     *
     * @param list the items
     * @return overriding methods should return true, or their sort will be undone
     */
    protected boolean preSort(List list)
    {
        return false;
    }

    public JComboBox getComboBox()
    {
        return m_comboBox;
    }

    private boolean isMandatory()
    {
        return getProperty().isMandatory();
    }

    private String getQuery()
    {
        return getProperty() != null ? getProperty().getQuery() : "";
    }

    private class CodeCompleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            JTextField tf = getTextField();
            final String text = tf.getText();
            final int caretIndex = tf.getCaretPosition();
            String pre = text.substring(0, caretIndex);
            java.util.List<TreeNode> nodes = m_root.search(pre);
            Collections.sort(nodes);
            if (nodes.isEmpty()) return;

            JPopupMenu jp = new JPopupMenu("options");

            JMenuItem lastItem = null;
            for (TreeNode node : nodes)
            {
                String nodeName = node.toString();
                String insertion = nodeName.substring(leaf(pre, m_root.pathSeparator()).length());
                lastItem = createInsertAction(nodeName, caretIndex, insertion);
                jp.add(lastItem);
            }
            if (nodes.size() == 0) return;
            if (nodes.size() == 1)
            {
                lastItem.getAction().actionPerformed(null);
            }
            else
            {
                Point pos = tf.getCaret().getMagicCaretPosition();
                pos = pos != null ? pos : new Point(2, 2);
                jp.show(tf, pos.x, pos.y);
            }
        }
    }

    private JTextField getTextField()
    {
        return (JTextField) m_comboBox.getEditor().getEditorComponent();
    }

    private JMenuItem createInsertAction(final String name, final int caretIndex, final String insertion)
    {
        JMenuItem mi = new JMenuItem(new AbstractAction(name)
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    getTextField().getDocument().insertString(caretIndex, insertion, null);
                }
                catch (BadLocationException e1)
                {
                    throw new XappException(e1);
                }
            }
        });
        mi.setFont(SwingUtils.DEFAULT_FONT);
        return mi;
    }

}
