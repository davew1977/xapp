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

public abstract class Key implements KeyChange
{
    private String m_clazz;
    private String m_key;

    public Key(String clazz, String key)
    {
        m_clazz = clazz;
        m_key = key;
    }

    public Key()
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

    public int compareTo(Object o)
    {
        return toString().compareTo(o.toString());
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (m_clazz != null ? !m_clazz.equals(key.m_clazz) : key.m_clazz != null) return false;
        if (m_key != null ? !m_key.equals(key.m_key) : key.m_key != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (m_clazz != null ? m_clazz.hashCode() : 0);
        result = 31 * result + (m_key != null ? m_key.hashCode() : 0);
        return result;
    }
}