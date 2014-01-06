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

import net.sf.xapp.annotations.objectmodelling.Transient;

/**
 * Encapsulates the diff of an object's primary key
 */
public class PrimaryKeyChange implements KeyChange
{
    private String m_clazz;
    private String m_old;
    private String m_new;
    private String m_initialValue;
    private boolean m_trackNewAndRemoved;

    public PrimaryKeyChange()
    {
    }

    public PrimaryKeyChange(String className, String old, String aNew)
    {
        this(className, old, aNew, false);
    }

    public PrimaryKeyChange(String className, String old, String aNew, boolean trackNewAndRemoved)
    {
        m_clazz = className;
        m_old = old;
        m_new = aNew;
        m_trackNewAndRemoved = trackNewAndRemoved;
    }

    public String getClazz()
    {
        return m_clazz;
    }

    public void setClazz(String clazz)
    {
        m_clazz = clazz;
    }

    public String getOld()
    {
        return m_old;
    }

    public void setOld(String old)
    {
        m_old = old;
    }

    public String getNew()
    {
        return m_new;
    }

    public void setNew(String aNew)
    {
        m_new = aNew;
    }

    @Transient
    public String getInitialValue()
    {
        return m_initialValue;
    }

    public void setInitialValue(String initialValue)
    {
        m_initialValue = initialValue;
    }

    public int compareTo(Object o)
    {
        return toString().compareTo(o.toString());
    }

    public boolean trackNewAndRemoved()
    {
        return m_trackNewAndRemoved;
    }

    @Override
    public String toString()
    {
        return m_clazz+": old = "+m_old + " , new = " + m_new;
    }
}
