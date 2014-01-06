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
package net.sf.xapp.application.editor;

import net.sf.xapp.application.api.ObjectWidget;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.ClassModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SpecialEditor implements Editor
{
    private ClassModel m_classModel;
    private Object m_instance;

    private ObjectWidget m_objectWidget;
    private JPanel m_mainPanel;
    private JFrame m_mainFrame;

    private EditorListener m_editorListener;
    private JPanel m_buttonPanel;
    private JButton m_saveButton;
    private JButton m_closeButton;

    SpecialEditor(ClassModel classModel, ObjectWidget objectWidget)
    {
        m_classModel = classModel;
        m_objectWidget = objectWidget;
        getMainFrame();
    }

    public void setGuiListener(EditorListener editorListener)
    {
        m_editorListener = editorListener;
    }

    public void setTarget(Object instance)
    {
        m_instance = instance;
        m_objectWidget.getFromObject(instance);
        updateFields();
    }

    public void setEditableContext(EditableContext editableContext)
    {
        setTarget(editableContext.getTarget());
    }

    public void setCloseOnSave(boolean closeOnSave)
    {

    }

    public Window getMainWindow()
    {
        return getMainFrame();
    }

    public boolean wasCancelled()
    {
        return false;
    }

    public JFrame getMainFrame()
    {
        if (m_mainFrame == null)
        {
            m_mainFrame = new JFrame(m_classModel.toString());
            m_mainFrame.setContentPane(getMainPanel());
            m_mainFrame.pack();
            m_mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            if (SwingUtils.DEFAULT_FRAME_ICON != null)
            {
                m_mainFrame.setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
            }
        }
        return m_mainFrame;
    }

    public JDialog getMainDialog(Frame owner, boolean modal)
    {
        return null;
    }

    private Container getMainPanel()
    {
        if (m_mainPanel == null)
        {
            m_mainPanel = new JPanel(new BorderLayout());
            m_mainPanel.add(m_objectWidget.getComponent(), "North");
            m_mainPanel.add(getButtonPanel(), "South");
            SwingUtils.setFont(m_mainPanel, SwingUtils.DEFAULT_FONT);
        }
        return m_mainPanel;
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
            m_closeButton = new JButton("Close");
            m_closeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getMainFrame().setVisible(false);
                }
            });

        }
        return m_closeButton;
    }

    private JButton getSaveButton()
    {
        if (m_saveButton == null)
        {
            m_saveButton = new JButton("Save");
            m_saveButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    m_objectWidget.setToObject(m_instance);
                    m_editorListener.save(null, true);
                    getMainFrame().setVisible(false);
                }
            });

        }
        return m_saveButton;
    }

    public void updateFields()
    {
        //do nothing
    }
}
