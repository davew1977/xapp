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
package net.sf.xapp.application.diff;

import net.sf.xapp.objectmodelling.difftracking.ComplexPropertyDiff;
import net.sf.xapp.objectmodelling.difftracking.Conflict;
import net.sf.xapp.objectmodelling.difftracking.DiffSet;

import java.util.List;

public class DiffModel
{
    private DiffSet m_baseToMine;
    private DiffSet m_baseToTheirs;
    private DiffSet m_mineToTheirs;

    public int m_newObjects;
    public int m_removedObjects;
    public int m_propertyChanges;
    public List<Conflict> m_conflicts;
    public int m_editsInRemovedObjects;

    public DiffModel(DiffSet mineToTheirs)
    {
        this(null,null,mineToTheirs);
    }

    public DiffModel(DiffSet baseToMine, DiffSet baseToTheirs, DiffSet mineToTheirs)
    {
        if(baseToMine!=null ^ baseToTheirs!=null) throw new IllegalArgumentException("baseToMine and baseToTheirs should both be set or both be null");
        m_baseToMine = baseToMine;
        m_baseToTheirs = baseToTheirs;
        m_mineToTheirs = mineToTheirs;

        m_newObjects = mineToTheirs.getNewNodeDiffs().size();
        m_removedObjects = mineToTheirs.getRemovedNodeDiffs().size();
        m_propertyChanges = mineToTheirs.getPropertyDiffs().size() + mineToTheirs.getReferenceListDiffs().size();

        for (ComplexPropertyDiff complexPropertyDiff : m_mineToTheirs.getComplexPropertyDiffs())
        {
            if (complexPropertyDiff.isRemoved()) m_removedObjects++;
            else if (complexPropertyDiff.getNewValue() != null) m_newObjects++;
            else //recursively assess this sub diffset
            {
                DiffSet diffSet = complexPropertyDiff.getDiffSet();
                DiffModel diffModel = new DiffModel(diffSet); //recursive
                m_newObjects += diffModel.m_newObjects;
                m_removedObjects += diffModel.m_removedObjects;
                m_propertyChanges += diffModel.m_propertyChanges;
            }
        }

        if (m_baseToMine!=null) //resolve conflicts
        {
            m_conflicts = m_baseToMine.findConflicts(m_baseToTheirs);
        }
    }

    public DiffSet getBaseToMine()
    {
        return m_baseToMine;
    }

    public DiffSet getBaseToTheirs()
    {
        return m_baseToTheirs;
    }

    public DiffSet getMineToTheirs()
    {
        return m_mineToTheirs;
    }
}
