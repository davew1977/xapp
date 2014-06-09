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

import net.sf.xapp.application.api.PropertyWidget;
import net.sf.xapp.application.api.WidgetContext;
import net.sf.xapp.application.editor.widgets.*;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * Default editor GUI created by introspecting the class
 */
public class DefaultEditor implements Editor
{
    private EditableContext m_editableContext;

    private Box m_compPanel;
    private Box m_mainPanel;
    private JFrame m_mainFrame;

    private Map<String, PropertyWidget> m_components;
    private EditorListener m_editorListener;
    private Box m_buttonPanel;
    private JButton m_saveButton;
    private JButton m_closeButton;
    private Component m_focusComp;
    public static final Color ERROR_COLOR = new Color(0xffbbbb);
    public static final Map<Class, Class> BOUND_COMPONENT_TYPES = new HashMap<Class, Class>();
    private boolean m_closeOnSave = true;
    private JDialog m_mainDialog;
    private boolean m_cancelled;

    DefaultEditor()
    {
        m_components = new HashMap<String, PropertyWidget>();
    }

    public void setEditableContext(EditableContext editableContext)
    {
        m_editableContext = editableContext;
        getMainPanel();
        updateFields();
        if (m_focusComp != null) m_focusComp.requestFocusInWindow();
    }

    public void setCloseOnSave(boolean closeOnSave)
    {
        m_closeOnSave = closeOnSave;
    }

    public void setGuiListener(EditorListener editorListener)
    {
        m_editorListener = editorListener;
    }

    public JFrame getMainFrame()
    {
        if (m_mainFrame == null)
        {
            m_mainFrame = new JFrame();
            m_mainFrame.setContentPane(getMainPanel());
            m_mainFrame.pack();
            m_mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            if (SwingUtils.DEFAULT_FRAME_ICON != null)
            {
                m_mainFrame.setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
            }
        }
        m_mainFrame.setTitle(m_editableContext.getTitle());
        return m_mainFrame;
    }

    public JDialog getMainDialog(Frame owner, boolean modal)
    {
        if(m_mainDialog==null)
        {
            m_mainDialog = new JDialog(owner, modal);
            m_mainDialog.setContentPane(getMainPanel());
            m_mainDialog.pack();
            //m_mainDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            if (SwingUtils.DEFAULT_FRAME_ICON != null)
            {
                //not 1.5 compatible m_mainDialog.setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
            }
            m_mainDialog.setTitle(m_editableContext.getTitle());
            m_mainDialog.setLocationRelativeTo(owner);
        }
        return m_mainDialog;
    }

    public Container getMainPanel()
    {
        if (m_mainPanel == null)
        {
            m_mainPanel = Box.createVerticalBox();
            JScrollPane jsp = new JScrollPane(getCompPanel());
            Dimension size = jsp.getPreferredSize();
            if (size.height > 500)
            {
                jsp.setPreferredSize(new Dimension(size.width + 50, 500));
            }
            m_mainPanel.add(jsp);
            m_mainPanel.add(getButtonPanel());
            //SwingUtils.setFont(m_mainPanel, SwingUtils.DEFAULT_FONT);
        }
        return m_mainPanel;
    }

    private Box getButtonPanel()
    {
        if (m_buttonPanel == null)
        {
            m_buttonPanel = Box.createHorizontalBox();
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
            m_closeButton.setAction(new CloseAction());
            m_closeButton.setFont(SwingUtils.DEFAULT_FONT);
        }
        return m_closeButton;
    }

    private JButton getSaveButton()
    {
        if (m_saveButton == null)
        {
            m_saveButton = new JButton();
            m_saveButton.setAction(new SaveAction());
            m_saveButton.setFont(SwingUtils.DEFAULT_FONT);
        }
        return m_saveButton;
    }

    private Container getCompPanel()
    {
        if (m_compPanel == null)
        {
            int spacing = 5;
            m_compPanel = new Box(BoxLayout.PAGE_AXIS);
            List<JLabel> labels = new ArrayList<JLabel>();
            List<PropertyWidget> propertyWidgets = new ArrayList<PropertyWidget>();
            //create labels and boundProperties
            int maxLabelWidth = 0;
            int maxComponentWidth = 0;
            int totalHeight = 0;
            List<Property> allProperties = m_editableContext.getVisibleProperties();
            for (Property property : allProperties)
            {
                JLabel label = new JLabel(property.getName());
                label.setFont(SwingUtils.DEFAULT_FONT);
                int width = label.getPreferredSize().width;
                maxLabelWidth = Math.max(maxLabelWidth, width);
                labels.add(label);
                PropertyWidget propertyWidget = createComponent(property);
                propertyWidget.init(new WidgetContextImpl(property));
                SwingUtils.setFont(propertyWidget.getComponent(), SwingUtils.DEFAULT_FONT);
                maxComponentWidth = Math.max(propertyWidget.getComponent().getPreferredSize().width, maxComponentWidth);
                propertyWidgets.add(propertyWidget);
            }

            for (int i = 0; i < allProperties.size(); i++)
            {
                Property property = allProperties.get(i);
                JLabel label = new JLabel(property.getName());
                label.setFont(SwingUtils.DEFAULT_FONT);
                label.setPreferredSize(new Dimension(maxLabelWidth, label.getPreferredSize().height));
                label.setMinimumSize(new Dimension(maxLabelWidth, label.getPreferredSize().height));
                label.setMaximumSize(new Dimension(maxLabelWidth, label.getPreferredSize().height));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                label.setAlignmentY(Component.TOP_ALIGNMENT);
                PropertyWidget propertyWidget = propertyWidgets.get(i);
                JComponent comp = propertyWidget.getComponent();
                comp.setAlignmentY(Component.TOP_ALIGNMENT);
                //label.setPreferredSize(new Dimension(maxLabelWidth, 20));
                int height = comp.getPreferredSize().height;
                totalHeight += height + spacing;
                comp.setPreferredSize(new Dimension(maxComponentWidth, height));
                comp.setMinimumSize(new Dimension(maxComponentWidth, height));
                //comp.setMaximumSize(new Dimension(maxComponentWidth, height));

                Box row = new Box(BoxLayout.LINE_AXIS);
                row.add(Box.createRigidArea(new Dimension(4, 4)));
                row.add(label);

                //row.add(Box.createHorizontalGlue());
                row.add(comp);
                row.add(Box.createRigidArea(new Dimension(4, 4)));

                if (i == 0) m_focusComp = comp;
                row.setPreferredSize(new Dimension(maxComponentWidth + maxLabelWidth + 15, height));
				row.setAlignmentX(Box.LEFT_ALIGNMENT);
                m_compPanel.add(row);
                m_components.put(property.getName(), propertyWidget);
            }
            //m_compPanel.add(Box.createVerticalGlue());
            //resolve the filter on properties
            for (final Property property : allProperties)
            {
                String filterOnProperty = property.getFilterOnProperty();
                if (filterOnProperty != null)
                {
                    final PropertyWidget propertyWidget = m_components.get(filterOnProperty);
                    final JComboBox comboBox = (JComboBox) propertyWidget.getComponent();
                    comboBox.addItemListener(new ItemListener()
                    {
                        public void itemStateChanged(ItemEvent e)
                        {
                            if (e.getStateChange() == ItemEvent.SELECTED)
                            {
                                Object selectedItem = comboBox.getSelectedItem();
                                ClassModel propertyClassModel = propertyWidget.getProperty().getPropertyClassModel();
                                Vector vector = propertyClassModel.search(selectedItem, property.getPropertyClass(), new HashSet());
                                PropertyWidget bc = m_components.get(property.getName());
                                JComboBox cb = (JComboBox) bc.getComponent();
                                cb.setModel(new DefaultComboBoxModel(vector));
                            }
                        }
                    });
                }
            }
            //m_compPanel.setPreferredSize(new Dimension(maxComponentWidth+maxLabelWidth+10,totalHeight+10));
        }
        return m_compPanel;
    }

    private PropertyWidget createComponent(Property property)
    {
        Class boundCompType = BOUND_COMPONENT_TYPES.get(property.getPropertyClass());
        if (property.hasSpecialBoundComponent())
        {
            return EditorUtils.createBoundProperty(property);
        }
        else if (boundCompType != null)
        {
            return EditorUtils.createBoundProperty(boundCompType);
        }
        else if (property.getPropertyClass().equals(String.class))
        {
            return new StringPropertyWidget();
        }
        else if (property.isDouble())
        {
            return new DoublePropertyWidget();
        }
        else if (property.isFloat())
        {
            return new FloatPropertyWidget();
        }
        else if (property.isInt())
        {
            return new IntegerPropertyWidget();
        }
        else if (property.isLong())
        {
            return new LongPropertyWidget();
        }
        else if (property.isBoolean())
        {
            return new BooleanPropertyWidget();
        }
        else if (property.getPropertyClass().isEnum())
        {
            return new EnumPropertyWidget();
        }
        else if (property.isReference())
        {
            return new ReferencePropertyWidget(false);
        }
        else if(property.getPropertyClass().equals(Date.class))
        {
            return new DatePropertyWidget();
        }
        else if (property instanceof ListProperty)
        {
            ListProperty lp = (ListProperty) property;
            if (lp.getContainedType().equals(Integer.class)) //special handling for 'primitives' - including String
            {
                return new IntegerListPropertyWidget();
            }
            if (lp.getContainedType().equals(String.class))
            {
                return new StringListPropertyWidget(lp.isSetCollection());
            }
            if(lp.getContainedType().equals(Long.class))
            {
                return new LongListPropertyWidget();
            }
            if(lp.getContainedType().isEnum())
            {
                return new EnumListPropertyWidget(lp.getContainedType());
            }
            return new NullPropertyWidget(property);
        }
        else if (property.getPropertyClass().equals(String[].class))
        {
            return new StringArrayPropertyWidget();
        }
        else if (property.getClassDatabase().getStringSerializer(property.getPropertyClass()) != null)
        {
            return new StringSerializablePropertyWidget(property);
        }
        //now see if there is a specialized bound component
        ClassModel classModel = property.getPropertyClassModel();
        PropertyWidget propertyWidget = EditorUtils.createBoundProperty(classModel, property, this);
        if (propertyWidget != null) return propertyWidget;

        //now create a bound component
        return new ObjectPropertyWidget(m_editableContext.getNodeUpdateApi());
    }

    public void updateFields()
    {
        for (PropertyWidget propertyWidget : m_components.values())
        {
            propertyWidget.setValue(m_editableContext.getPropertyValue(propertyWidget.getProperty()), m_editableContext.getObjMeta());
            propertyWidget.setEditable(m_editableContext.isPropertyEditable(propertyWidget.getProperty()));
            if (propertyWidget.getComponent().getBackground().equals(ERROR_COLOR))
            {
                propertyWidget.getComponent().setBackground(Color.WHITE);
            }
        }
    }

    private static class NullPropertyWidget implements PropertyWidget
    {
        private final Property m_property;
        private Object m_value;

        public NullPropertyWidget(Property property)
        {
            m_property = property;
        }

        public JComponent getComponent()
        {
            return new JLabel(m_property.getPropertyClassModel() + " not supported");
        }

        public Object getValue()
        {
            return m_value;
        }

        public void setValue(Object value, ObjectMeta target)
        {
            m_value = value;
        }

        public Property getProperty()
        {
            return m_property;
        }

        public void init(WidgetContext context)
        {

        }

        public String validate()
        {
            return null;
        }

        public void setEditable(boolean editable)
        {

        }
    }

    private class SaveAction extends AbstractAction
    {
        public SaveAction()
        {
            super("Save");
            putValue(MNEMONIC_KEY, new Integer('S'));
        }

        public void actionPerformed(ActionEvent e)
        {
            //validation
            if (m_editableContext.isValidateFields() && containsErrors()) return;

            if (m_editableContext.isCheckMandatoryFields() && !areMandatoryFieldsFilled()) return; // short circuit


            List<PropertyUpdate> changes = new ArrayList<PropertyUpdate>();

            for (PropertyWidget propertyWidget : m_components.values())
            {
                //deal with bug on mac which adds a ß at the end of a field on save
                if(propertyWidget.getComponent() instanceof JTextField) {
                    JTextField component = (JTextField) propertyWidget.getComponent();
                    String text = component.getText();
                    if(text != null && text.endsWith("ß")) {
                        component.setText(text.substring(0, text.length() - 1));
                    }

                }
                changes.addAll(m_editableContext.potentialUpdates(propertyWidget.getProperty(), propertyWidget.getValue()));
            }

            if (m_closeOnSave)
            {
                getMainWindow().setVisible(false);
            }

            m_cancelled = false;
            m_editorListener.save(changes, m_closeOnSave);

        }

        private boolean containsErrors()
        {
            boolean errors = false;
            for (PropertyWidget propertyWidget : m_components.values())
            {
                String error = propertyWidget.validate();
                if (error != null)
                {
                    propertyWidget.getComponent().setBackground(Color.RED);
                    JOptionPane.showMessageDialog(getSaveButton(), propertyWidget.getProperty().getName() + ": " + error, "Validation Error", JOptionPane.ERROR_MESSAGE);
                    propertyWidget.getComponent().setBackground(ERROR_COLOR);
                    errors = true;
                }
            }
            return errors;
        }

        private boolean areMandatoryFieldsFilled()
        {
            List<PropertyWidget> missingMandatoryProps = new ArrayList<PropertyWidget>();
            for (PropertyWidget propertyWidget : m_components.values())
            {
                if (propertyWidget.getProperty().isMandatory() && propertyWidget.getValue() == null)
                {
                    missingMandatoryProps.add(propertyWidget);
                }
            }
            if (!missingMandatoryProps.isEmpty())
            {
                setBackground(missingMandatoryProps, Color.RED);
                JOptionPane.showMessageDialog(getSaveButton(), "Mandatory field" + (missingMandatoryProps.size() == 1 ? "" : "s") + " not filled in", "Validation Error", JOptionPane.ERROR_MESSAGE);
                setBackground(missingMandatoryProps, ERROR_COLOR);
                return false;
            }
            return true;
        }

        private void setBackground(List<PropertyWidget> invalidProps, Color color)
        {
            for (PropertyWidget invalidProp : invalidProps)
            {
                invalidProp.getComponent().setBackground(color);
            }
        }
    }

    public Window getMainWindow()
    {
        return m_mainFrame!=null ? m_mainFrame : m_mainDialog != null ? m_mainDialog : getMainFrame();
    }

    public boolean wasCancelled()
    {
        return m_cancelled;
    }

    private class WidgetContextImpl implements WidgetContext
    {
        private Property m_property;

        private WidgetContextImpl(Property property)
        {
            m_property = property;
        }

        public EditMode getEditMode()
        {
            if (m_editableContext instanceof MultiTargetEditableContext) return EditMode.MULTI_EDIT;
            return ((SingleTargetEditableContext) m_editableContext).getMode() == SingleTargetEditableContext.Mode.EDIT ? EditMode.EDIT : EditMode.CREATE;
        }

        public ClassDatabase getClassDatabase()
        {
            return m_editableContext.getClassModel().getClassDatabase();
        }

        public Property getProperty()
        {
            return m_property;
        }

        public String getArgs()
        {
            return m_property.getBoundPropertyArgs();
        }

        public Editor getEditor()
        {
            return DefaultEditor.this;
        }

        public ClassModel getPropertyClassModel()
        {
            return getProperty().getPropertyClassModel();
        }

        @Override
        public String tooltipMethod()
        {
            return getProperty().getReference().tooltipMethod();
    }
    }

    private class CloseAction extends AbstractAction
    {
        public CloseAction()
        {
            super("Close");
            putValue(MNEMONIC_KEY, new Integer('C'));
        }

        public void actionPerformed(ActionEvent e)
        {
            m_cancelled = true;
            m_editorListener.close();
            getMainWindow().setVisible(false);
        }
    }
}
