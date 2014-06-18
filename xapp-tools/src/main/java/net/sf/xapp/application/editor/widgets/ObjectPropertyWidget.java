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

import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ObjectPropertyWidget extends AbstractPropertyWidget
{
    private NodeUpdateApi nodeUpdateApi;
    private Object m_propertyValue;
    private JPanel m_mainPanel;
    private JButton m_editButton;
    private JButton m_createButton;
    private JButton m_removeButton;
    private JPopupMenu m_popUp;
    private boolean m_editable;
    private ObjectMeta parentObj;

    public ObjectPropertyWidget(NodeUpdateApi nodeUpdateApi)
    {
        this.nodeUpdateApi = nodeUpdateApi;
        m_mainPanel = getMainPanel();
        updateState();
    }

    private JPanel getMainPanel()
    {
        if (m_mainPanel == null)
        {
            m_mainPanel = new JPanel();
            m_mainPanel.add(getCreateButton());
            m_mainPanel.add(getEditButton());
            m_mainPanel.add(getRemoveButton());
        }
        return m_mainPanel;
    }

    private JButton getRemoveButton()
    {
        if (m_removeButton == null)
        {
            m_removeButton = createButton("Remove");
            m_removeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    m_propertyValue = null;
                    updateState();
                }
            });
        }
        return m_removeButton;
    }

    private JButton createButton(String name)
    {
        JButton jButton = new JButton(name);
        jButton.setFont(SwingUtils.DEFAULT_FONT);
        //jButton.setPreferredSize(new Dimension(50, 15));
        jButton.setHorizontalAlignment(JButton.LEFT);
        jButton.setMargin(new Insets(2, 2, 2, 2));
        return jButton;
    }

    private JButton getEditButton()
    {
        if (m_editButton == null)
        {
            m_editButton = createButton("Edit");
            m_editButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ClassModel propertyClassModel = m_widgetContext.getClassDatabase().getClassModel(m_propertyValue.getClass());
                    final ObjectMeta objValue = propertyClassModel.find(m_propertyValue);
                    EditableContext editableContext = new SingleTargetEditableContext(objValue, SingleTargetEditableContext.Mode.EDIT, nodeUpdateApi);
                    Editor defaultEditor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
                    {
                        public void save(java.util.List<PropertyUpdate> changes, boolean closing)
                        {
                            updateState();
                            nodeUpdateApi.updateObject(objValue, changes);
                        }
                    });
                    defaultEditor.getMainFrame().setLocationRelativeTo(m_mainPanel);
                    defaultEditor.getMainFrame().setVisible(true);
                }
            });
        }
        return m_editButton;
    }

    private JButton getCreateButton()
    {
        if (m_createButton == null)
        {
            m_createButton = createButton("Create");
            m_createButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ClassModel propertyClassModel = m_widgetContext.getProperty().getPropertyClassModel();
                    if (propertyClassModel.isAbstract())
                    {
                        getPopUpMenu(propertyClassModel).show(getCreateButton(), 0, 0);
                    }
                    else
                    {
                        doCreateObject(propertyClassModel);
                    }
                }
            });
        }
        return m_createButton;
    }

    private JPopupMenu getPopUpMenu(ClassModel propertyClassModel)
    {
        if (m_popUp == null)
        {
            m_popUp = new JPopupMenu();
            java.util.List<ClassModel> validImpls = propertyClassModel.getValidImplementations();
            for (final ClassModel validImpl : validImpls)
            {
                JMenuItem menuItem = new JMenuItem(validImpl.getSimpleName());
                menuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        doCreateObject(validImpl);
                    }
                });
                m_popUp.add(menuItem);
            }
        }
        return m_popUp;
    }

    private void doCreateObject(ClassModel validImpl)
    {
        final ObjectMeta instance = nodeUpdateApi.createObject(new ObjectLocation(parentObj, getProperty()), validImpl);
        EditableContext editableContext = new SingleTargetEditableContext(instance, SingleTargetEditableContext.Mode.CREATE, nodeUpdateApi);
        Editor defaultEditor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
        {
            public void save(List<PropertyUpdate> changes, boolean closing)
            {
                updateState();
                nodeUpdateApi.initObject(instance, changes);
            }

            @Override
            public void close() {
                nodeUpdateApi.deleteObject(instance);
            }
        });
        defaultEditor.getMainFrame().setLocationRelativeTo(m_mainPanel);
        defaultEditor.getMainFrame().setVisible(true);
    }

    public JComponent getComponent()
    {
        return m_mainPanel;
    }

    public Object getValue()
    {
        return m_propertyValue;
    }

    public void setValue(Object value, ObjectMeta target)
    {
        m_propertyValue = value;
        parentObj = target;
        updateState();
    }

    private void updateState()
    {
        getCreateButton().setEnabled(m_editable && m_propertyValue == null);
        getRemoveButton().setEnabled(m_editable && m_propertyValue != null);
        getEditButton().setEnabled(m_editable && m_propertyValue != null);
        getRemoveButton().setToolTipText(m_propertyValue != null ? m_propertyValue.toString() : null);
        getEditButton().setToolTipText(m_propertyValue != null ? m_propertyValue.toString() : null);
    }

    public void setEditable(boolean editable)
    {
        m_editable = editable;
        updateState();
    }
}
