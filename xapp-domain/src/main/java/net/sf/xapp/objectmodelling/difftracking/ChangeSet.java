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

import net.sf.xapp.annotations.objectmodelling.ListType;

import java.util.ArrayList;
import java.util.List;

public class ChangeSet
{
    private List<PrimaryKeyChange> m_primaryKeyChanges = new ArrayList<PrimaryKeyChange>();
    private List<NewKey> m_newKeys = new ArrayList<NewKey>();
    private List<RemovedKey> m_removedKeys = new ArrayList<RemovedKey>();

    @ListType(PrimaryKeyChange.class)
    public List<PrimaryKeyChange> getPrimaryKeyChanges()
    {
        return m_primaryKeyChanges;
    }

    public void setPrimaryKeyChanges(List<PrimaryKeyChange> primaryKeyChanges)
    {
        m_primaryKeyChanges = primaryKeyChanges;
    }

    @ListType(NewKey.class)
    public List<NewKey> getNewKeyChanges()
    {
        return m_newKeys;
    }


    public void setNewKeyChanges(List<NewKey> newKeys)
    {
        m_newKeys = newKeys;
    }

    @ListType(RemovedKey.class)
    public List<RemovedKey> getRemovedKeyChanges()
    {
        return m_removedKeys;
    }

    public void setRemovedKeyChanges(List<RemovedKey> removedKeys)
    {
        m_removedKeys = removedKeys;
    }
}
