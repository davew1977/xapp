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

import java.util.List;

public class Conflict
{
    private Diff m_thisDiff;
    private Diff m_otherDiff;
    private ConflictType m_conflictType;
    private List<Conflict> m_subConflicts;

    public Conflict(Diff thisDiff, Diff otherDiff, ConflictType conflictType)
    {
        this(thisDiff, otherDiff, conflictType, null);
    }

    public Conflict(Diff thisDiff, Diff otherDiff, ConflictType conflictType, List<Conflict> conflicts)
    {
        m_thisDiff = thisDiff;
        m_otherDiff = otherDiff;
        m_conflictType = conflictType;
        m_subConflicts = conflicts;
    }

    public Diff getThisDiff()
    {
        return m_thisDiff;
    }

    public Diff getOtherDiff()
    {
        return m_otherDiff;
    }

    public ConflictType getType()
    {
        return m_conflictType;
    }

    public List<Conflict> getSubConflicts()
    {
        return m_subConflicts;
    }
}
