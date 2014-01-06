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

public class KeyChanges
{
    public boolean m_added;
    public boolean m_removed;
    public boolean m_changed;

    public boolean addedOnly()
    {
        return m_added && !m_removed && !m_changed;
    }

    public boolean removedOnly()
    {
        return m_removed && !m_added && !m_changed;
    }

    public boolean any()
    {
        return m_removed || m_added || m_changed;
    }

    public void orEquals(KeyChanges keyChanges)
    {
        m_added|=keyChanges.m_added;
        m_changed|=keyChanges.m_changed;
        m_removed|=keyChanges.m_removed;

    }
}
