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

import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.objectmodelling.core.Property;

import javax.swing.*;
import java.awt.*;

public class StringSerializablePropertyWidget extends AbstractPropertyWidget
{
    protected Property m_property;
    protected JTextField m_textField;
    protected StringSerializer m_stringSerializer;

    public StringSerializablePropertyWidget(Property property)
    {
        m_property = property;
        m_textField = new JTextField(14);
        m_stringSerializer = property.getClassDatabase().getStringSerializer(property.getPropertyClass());
        if (m_stringSerializer == null) throw new IllegalArgumentException(property + " has no string serializer!");
        m_textField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

    }

    public JComponent getComponent()
    {
        return m_textField;
    }


    public Object getValue()
    {
        String text = m_textField.getText();
        return "".equals(text) ? null : m_stringSerializer.read(text);
    }
                                                                                                                     
    public void setValue(Object value, Object target)
    {
        m_textField.setText(value != null ? m_stringSerializer.write(value) : null);
    }

    public String validate()
    {
        String text = m_textField.getText();
        if(text.equals(""))return null;
        return m_stringSerializer.validate(text);
    }

    public void setEditable(boolean editable)
    {
        m_textField.setEditable(editable);
    }
}
