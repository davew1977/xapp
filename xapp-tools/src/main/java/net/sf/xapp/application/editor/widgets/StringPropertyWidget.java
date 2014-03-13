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

import net.sf.xapp.annotations.application.Validate;
import net.sf.xapp.application.api.WidgetContext;

import javax.swing.*;
import java.awt.*;

public class StringPropertyWidget extends AbstractPropertyWidget
{
    protected JTextField m_textField;

    public StringPropertyWidget()
    {
        m_textField = new JTextField();
    }

    public Object getValue()
    {
        return m_textField.getText().equals("") ? null : m_textField.getText();
    }

    public void setValue(Object value, Object target)
    {
        m_textField.setText(value!=null ? value.toString() : null);
    }

    public JComponent getComponent()
    {
        return m_textField;
    }

    public void init(WidgetContext context)
    {
        super.init(context);
        m_textField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        m_textField.setPreferredSize(new Dimension(120, 20));
    }

    public void setEditable(boolean editable)
    {
        m_textField.setEditable(editable);
    }

    @Override
    public String validate() {
        Validate validate = getProperty().getValidate();
        if(validate != null) {
            String regexp = validate.regexp();
            if(getValue() != null && !((String) getValue()).matches(regexp)) {
                return !validate.errorMsg().isEmpty() ? validate.errorMsg() :
                        " must match regexp: " + regexp;
            }
        }
        return null;
    }
}
