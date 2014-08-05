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

import java.util.ArrayList;
import java.util.List;

/**
 * "bean" storing a test script and meta
 */
public class Script implements Cloneable
{
    @Key
    @PropertyOrder(-1)
    private String m_name;

    @EditorWidget(value = ScriptWidget.class)
    private String m_content;

    @EditorWidget(FreeTextPropertyWidget.class)
    private String m_description;

    private List<String> m_paramMeta = new ArrayList<String>();

    public Script() {
    }

    public String getName()
    {
        return m_name;
    }

    public String getContent()
    {
        return m_content;
    }

    public String getDescription()
    {
        return m_description;
    }

    public Script clone() throws CloneNotSupportedException
    {
        return (Script) super.clone();
    }

    public String toString()
    {
        return m_name;
    }

    public List<String> getParamMeta()
    {
        return m_paramMeta;
    }
}