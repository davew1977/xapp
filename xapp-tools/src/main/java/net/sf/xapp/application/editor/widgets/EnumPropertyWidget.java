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
import net.sf.xapp.marshalling.stringserializers.EnumListSerializer;
import net.sf.xapp.utils.XappException;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class EnumPropertyWidget extends AbstractPropertyWidget
{
    private JComboBox m_comboBox;

    public void setEditable(boolean editable)
    {
        m_comboBox.setEnabled(editable);
    }

    public JComponent getComponent()
    {
        return m_comboBox;
    }

    public Object getValue()
    {
        return m_comboBox.getSelectedItem();
    }

    public void setValue(Object value, Object target)
    {
        m_comboBox.setSelectedItem(value);

    }

    public void init(WidgetContext context)
    {
        super.init(context);

        //get Options
        Class propertyClass = m_widgetContext.getProperty().getPropertyClass();
        EnumListSerializer.getEnumValues(propertyClass);
        m_comboBox = new JComboBox(EnumListSerializer.getEnumValues(propertyClass));
        m_comboBox.setPreferredSize(new Dimension(m_comboBox.getPreferredSize().width, 20));
        m_comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
    }
}
