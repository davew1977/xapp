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
package net.sf.xapp.objectmodelling.difftracking;

public class PropertyDiff implements Diff, Cloneable
{
    protected String m_clazz;
    protected String m_key;
    protected String m_property;
    private String m_oldValue;
    private String m_newValue;

    public PropertyDiff(String className, String key, String propertyName, String oldValue, String newValue)
    {
        m_clazz = className;
        m_key = key;
        m_property = propertyName;
        m_oldValue = oldValue;
        m_newValue = newValue;
    }

    public PropertyDiff()
    {
    }

    public String getClazz()
    {
        return m_clazz;
    }

    public void setClazz(String clazz)
    {
        m_clazz = clazz;
    }

    public String getKey()
    {
        return m_key;
    }

    public void setKey(String key)
    {
        m_key = key;
    }

    public String getProperty()
    {
        return m_property;
    }

    public void setProperty(String property)
    {
        m_property = property;
    }

    public String getOldValue()
    {
        return m_oldValue;
    }

    public void setOldValue(String oldValue)
    {
        m_oldValue = oldValue;
    }

    public String getNewValue()
    {
        return m_newValue;
    }

    public void setNewValue(String newValue)
    {
        m_newValue = newValue;
    }

    public String createKey()
    {
        return m_clazz+":"+m_key+":"+m_property;
    }

    public PropertyDiff clone()
    {
        try
        {
            return (PropertyDiff) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
