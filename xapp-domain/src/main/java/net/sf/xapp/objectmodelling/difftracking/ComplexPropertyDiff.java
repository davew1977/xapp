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

import net.sf.xapp.annotations.marshalling.FormattedText;
import net.sf.xapp.annotations.objectmodelling.Transient;

public class ComplexPropertyDiff extends PropertyDiff
{
    private boolean m_removed;
    private DiffSet m_diffSet;
    private String m_propertyClass;

    public ComplexPropertyDiff(String className, String key, String propertyName, String oldValue, String newValue, boolean removed, DiffSet diffSet, String propertyClassName)
    {
        super(className, key, propertyName, oldValue, newValue);
        m_removed = removed;
        m_diffSet = diffSet;
        m_propertyClass = propertyClassName;
    }

    public ComplexPropertyDiff()
    {
    }

    @FormattedText
    public String getNewValue()
    {
        return super.getNewValue();
    }

    public boolean isRemoved()
    {
        return m_removed;
    }

    public void setRemoved(boolean removed)
    {
        m_removed = removed;
    }

    public DiffSet getDiffSet()
    {
        return m_diffSet;
    }

    public void setDiffSet(DiffSet diffSet)
    {
        m_diffSet = diffSet;
    }

    public String getPropertyClass()
    {
        return m_propertyClass;
    }

    public void setPropertyClass(String propertyClass)
    {
        m_propertyClass = propertyClass;
    }

    @Transient
    public boolean isNew()
    {
        return getNewValue()!=null;
    }

    public boolean hasDiffSet()
    {
        return m_diffSet!=null;
    }
}
