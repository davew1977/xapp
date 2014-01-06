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

import net.sf.xapp.objectmodelling.difftracking.Conflict;

import java.util.ArrayList;
import java.util.List;

public class SummaryBean
{
    public int m_newObjects;
    public int m_removedObjects;
    public int m_propertyChanges;
    public List<Conflict> m_conflicts = new ArrayList<Conflict>();

    public SummaryBean()
    {
    }

    public SummaryBean(DiffModel diffModel)
    {
        m_newObjects = diffModel.m_newObjects;
        m_removedObjects = diffModel.m_removedObjects;
        m_propertyChanges = diffModel.m_propertyChanges;
        m_conflicts = diffModel.m_conflicts;
    }
}
