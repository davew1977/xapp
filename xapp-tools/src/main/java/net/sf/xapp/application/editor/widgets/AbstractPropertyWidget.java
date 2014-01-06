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

import net.sf.xapp.application.api.PropertyWidget;
import net.sf.xapp.application.api.WidgetContext;
import net.sf.xapp.objectmodelling.core.Property;

public abstract class AbstractPropertyWidget<T> implements PropertyWidget<T>
{
    protected WidgetContext<T> m_widgetContext;

    public void init(WidgetContext<T> context)
    {
        m_widgetContext = context;
    }

    public final Property<T> getProperty()
    {
        return m_widgetContext!=null ? m_widgetContext.getProperty() : null;
    }

    public String validate()
    {
        return null;
    }
}
