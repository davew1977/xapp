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

import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.utils.ReflectionUtils;

public class ClassPropertyWidget extends StringPropertyWidget
{
    public Class getValue()
    {
        String s = (String) super.getValue();
        return s!=null ? ReflectionUtils.classForName(s) : null;
    }

    @Override
    public void setValue(Object value, ObjectMeta target) {
        super.setValue(value != null ? ((Class)value).getName() : null, target);
    }

    public String validate()
    {
        return validateInt(m_textField.getText());
    }

    public static String validateInt(String t)
    {
        try {
            Class.forName(t);
            return null;
        }
        catch (ClassNotFoundException e) {
            return e.getMessage();
        }
    }
}
