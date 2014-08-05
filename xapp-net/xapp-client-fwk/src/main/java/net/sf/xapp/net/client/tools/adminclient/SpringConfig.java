/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.PropertyOrder;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;

public class SpringConfig
{
    @Key
    @PropertyOrder(1)
    public String m_name;
    @PropertyOrder(2)
    public String m_filePaths;

    @EditorWidget(FreeTextPropertyWidget.class)
    @PropertyOrder(3)
    public String m_description;

    public String toString()
    {
        return m_name;
    }

    public String tooltip()
    {
        return m_description != null ?
                String.format("<html>%s</html>", m_description.replace("\n", "<br>")) : null;
    }
}