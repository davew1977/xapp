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
package net.sf.xapp.application.api;

import net.sf.xapp.application.editor.EditMode;
import net.sf.xapp.application.editor.Editor;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ListProperty;
import net.sf.xapp.objectmodelling.core.Property;

public class DummyWidgetContext implements WidgetContext
{
    private ClassModel m_propertyCM;
    private ListProperty m_listProperty;

    public DummyWidgetContext(ClassModel propertyCM, ListProperty listProperty)
    {
        m_propertyCM = propertyCM;
        m_listProperty = listProperty;
    }

    public EditMode getEditMode()
    {
        return null;
    }

    public ClassDatabase getClassDatabase()
    {
        return m_propertyCM.getClassDatabase();
    }

    public Property getProperty()
    {
        return m_listProperty;
    }

    public String getArgs()
    {
        return null;
    }

    public Editor getEditor()
    {
        return null;
    }

    public ClassModel getPropertyClassModel()
    {
        return m_propertyCM;
    }

    public String tooltipMethod()
    {
        return null;
}
}
