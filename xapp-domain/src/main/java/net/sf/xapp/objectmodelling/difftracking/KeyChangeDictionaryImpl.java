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

import java.util.*;

public class KeyChangeDictionaryImpl implements KeyChangeDictionary
{
    private Map<String, Map<String, PrimaryKeyChange>> m_primaryKeyChanges;
    private Set<NewKey> m_newKeys;
    private Set<RemovedKey> m_removedKeys;

    public KeyChangeDictionaryImpl()
    {
        m_primaryKeyChanges = new HashMap<String, Map<String, PrimaryKeyChange>>();
        m_newKeys = new HashSet<NewKey>();
        m_removedKeys = new HashSet<RemovedKey>();
    }

    public void init(ChangeSet changeSet)
    {
        for (PrimaryKeyChange change : changeSet.getPrimaryKeyChanges())
        {
            register(change);
        }
        m_newKeys = new HashSet<NewKey>(changeSet.getNewKeyChanges());
        m_removedKeys = new HashSet<RemovedKey>(changeSet.getRemovedKeyChanges());
    }

    public void primaryKeyChange(PrimaryKeyChange change)
    {
        //if new then remove any old changes registered
        if (change.getOld() == null || change.getOld().equals(""))
        {
            remove(change.getClazz(), change.getNew());
            if (change.trackNewAndRemoved())
            {
                registerNew(change);
            }
        }
        else
        {
            register(change);
        }
    }

    public void objectRemoved(String className, String key)
    {
        objectRemoved(className, key, false);
    }

    public void objectRemoved(String className, String key, boolean trackNewAndRemoved)
    {
        remove(className, key);
        if (trackNewAndRemoved)
        {
            registerRemoved(className, key);
        }
    }

    private void registerRemoved(String className, String key)
    {
        boolean wasNew = m_newKeys.remove(new NewKey(className, key));
        if(!wasNew)
        {
            m_removedKeys.add(new RemovedKey(className, key));
        }
    }

    public boolean isEmpty()
    {
        if(!m_newKeys.isEmpty() || !m_removedKeys.isEmpty())
        {
            return false;
        }
        for (Map<String, PrimaryKeyChange> changeMap : m_primaryKeyChanges.values())
        {
            if (!changeMap.isEmpty()) return false;
        }
        return true;
    }

    public ChangeSet createChangeSet()
    {
        List<PrimaryKeyChange> changes = new ArrayList<PrimaryKeyChange>();
        for (Map.Entry<String, Map<String, PrimaryKeyChange>> entry : m_primaryKeyChanges.entrySet())
        {
            for (PrimaryKeyChange keyChange : entry.getValue().values())
            {
                changes.add(keyChange);
            }
        }
        List<NewKey> newKeys = new ArrayList<NewKey>(m_newKeys);
        List<RemovedKey> removedKeys = new ArrayList<RemovedKey>(m_removedKeys);
        Collections.sort(changes); //make order deterministic
        Collections.sort(newKeys);
        Collections.sort(removedKeys);
        ChangeSet cm = new ChangeSet();
        cm.setPrimaryKeyChanges(changes);
        cm.setNewKeyChanges(newKeys);
        cm.setRemovedKeyChanges(removedKeys);
        return cm;
    }

    public PrimaryKeyChange findByOld(String className, String oldValue)
    {
        for (PrimaryKeyChange change : classChangeMap(className).values())
        {
            if (change.getOld().equals(oldValue))
            {
                return change;
            }
        }
        return null;
    }

    public PrimaryKeyChange findByNew(String className, String newValue)
    {
        return classChangeMap(className).get(newValue);
    }

    private void remove(String className, String key)
    {
        classChangeMap(className).remove(key);
    }

    private void register(PrimaryKeyChange change)
    {
        Map<String, PrimaryKeyChange> changeMap = classChangeMap(change.getClazz());
        PrimaryKeyChange previousRegistered = changeMap.remove(change.getOld());
        if (previousRegistered != null)
        {
            //if key changed back to what it was then do nothing more
            if (previousRegistered.getOld().equals(change.getNew()))
            {
                return;
            }
            //simplify the two changes into one
            change.setOld(previousRegistered.getOld());
        }
        changeMap.put(change.getNew(), change);
    }

    private void registerNew(PrimaryKeyChange primaryKeyChange)
    {
        String clazz = primaryKeyChange.getClazz();
        String newKey = primaryKeyChange.getNew();
        boolean wasRemoved = m_removedKeys.remove(new RemovedKey(clazz, newKey));
        if(!wasRemoved)
        {
            m_newKeys.add(new NewKey(clazz, newKey));
        }
    }


    private Map<String, PrimaryKeyChange> classChangeMap(String className)
    {
        Map<String, PrimaryKeyChange> classChangeMap = m_primaryKeyChanges.get(className);
        if (classChangeMap == null)
        {
            classChangeMap = new HashMap<String, PrimaryKeyChange>();
            m_primaryKeyChanges.put(className, classChangeMap);
        }
        return classChangeMap;
    }

    /*private Map<String, NewKey> classNewMap(String className)
    {
        Map<String, NewKey> classNewMap = m_newKeys.get(className);
        if (classNewMap == null)
        {
            classNewMap = new HashMap<String, NewKey>();
            m_newKeys.put(className, classNewMap);
        }
        return classNewMap;
    }

    private Map<String, RemovedKey> classRemovedMap(String className)
    {
        Map<String, RemovedKey> classRemovedMap = m_removedKeys.get(className);
        if (classRemovedMap == null)
        {
            classRemovedMap = new HashMap<String, RemovedKey>();
            m_removedKeys.put(className, classRemovedMap);
        }
        return classRemovedMap;
    }*/

    Map<String, Map<String, PrimaryKeyChange>> getPrimaryKeyChanges()
    {
        return m_primaryKeyChanges;
    }

    public Set<NewKey> getNewKeys()
    {
        return m_newKeys;
    }

    public Set<RemovedKey> getRemovedKeys()
    {
        return m_removedKeys;
    }
}
