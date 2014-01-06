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
package net.sf.xapp.objectmodelling.core;

/**
 * tuple including a property bound to an instance
 */
public class PropertyObjectPair
{
    private final Property m_property;
    private final Object m_target;


    public PropertyObjectPair(Property property, Object target)
    {
        if(property==null) throw new IllegalArgumentException("property is null");
        if(target ==null) throw new IllegalArgumentException("target is null");
        m_property = property;
        m_target = target;
    }

    public String getName()
    {
        return m_property.getName();
    }

    public Property getProperty()
    {
        return m_property;
    }

    public Object getTarget()
    {
        return m_target;
    }

    public void set(Object value)
    {
        m_property.set(m_target, value);
    }

    public Object get()
    {
        return m_property.get(m_target);
    }

    /**
     * Sets the property after decoding it from the string value
     * @param value the value as a string
     */
    public void setSpecial(String value)
    {
        m_property.setSpecial(m_target, value);
    }

    public String toString()
    {
        return m_target+"."+m_property.getName();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyObjectPair that = (PropertyObjectPair) o;

        if (!m_property.equals(that.m_property)) return false;
        if (!m_target.equals(that.m_target)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = m_property.hashCode();
        result = 31 * result + m_target.hashCode();
        return result;
    }
}
