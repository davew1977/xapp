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

import javax.swing.*;

public class BooleanPropertyWidget extends AbstractPropertyWidget
{
    protected JCheckBox m_checkBox;
    protected Box m_box;

    public BooleanPropertyWidget()
    {
        m_checkBox = new JCheckBox();
        m_box = Box.createHorizontalBox();
        m_box.add(m_checkBox);
        m_box.add(Box.createHorizontalGlue());
    }

    public Object getValue()
    {
        return m_checkBox.isSelected();
    }

    public void setEditable(boolean editable)
    {
        m_checkBox.setEnabled(editable);
    }

    public void setValue(Object value, Object target)
    {
        boolean v = value!=null && (Boolean) value;
        m_checkBox.setSelected(v);
    }

    public JComponent getComponent()
    {
        return m_box;
    }
}
