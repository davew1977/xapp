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

import net.sf.xapp.objectmodelling.api.ClassDatabase;

import java.util.ArrayList;
import java.util.List;

public class KeyChangeHistoryImpl implements KeyChangeHistory
{
    private List<KeyChangeDictionary> m_dictionaries;
    private KeyChangeDictionary m_currentKeyChangeDictionary;

    public KeyChangeHistoryImpl()
    {
        m_dictionaries = new ArrayList<KeyChangeDictionary>();
        m_currentKeyChangeDictionary = new KeyChangeDictionaryImpl();
    }

    public void init(ChangeModel changeModel)
    {
        List<ChangeSet> changeSets = changeModel.getChangeSets();
        for (ChangeSet changeSet : changeSets)
        {
            KeyChangeDictionary keyChangeDictionary = new KeyChangeDictionaryImpl();
            keyChangeDictionary.init(changeSet);
            m_dictionaries.add(keyChangeDictionary);
        }
    }

    public String resolveKey(String className, String key)
    {
        for (int i = m_dictionaries.size()-1; i >= 0; i--)
        {
            KeyChangeDictionary dictI = m_dictionaries.get(i);
            PrimaryKeyChange changeByOld = dictI.findByOld(className, key);
            if(changeByOld!=null)
            {
                for (int j = i+1; j < m_dictionaries.size(); j++)
                {
                    KeyChangeDictionary dictJ =  m_dictionaries.get(j);
                    PrimaryKeyChange change = dictJ.findByOld(className, changeByOld.getNew());
                    if(change!=null)
                    {
                        changeByOld = change;
                    }
                }
                return changeByOld.getNew();
            }
        }
        return null;
    }

    public boolean isEmpty()
    {
        for (KeyChangeDictionary dictionary : m_dictionaries)
        {
            if(!dictionary.isEmpty()) return false;
        }
        return m_currentKeyChangeDictionary.isEmpty();
    }

    public ChangeModel createChangeModel()
    {
        ChangeModel changeModel = new ChangeModel();
        changeModel.setChangeSets(new ArrayList<ChangeSet>());
        for (KeyChangeDictionary dictionary : m_dictionaries)
        {
            changeModel.getChangeSets().add(dictionary.createChangeSet());
        }
        if(!m_currentKeyChangeDictionary.isEmpty())
        {
            changeModel.getChangeSets().add(m_currentKeyChangeDictionary.createChangeSet());
        }
        return changeModel;
    }

    public KeyChangeDictionary getCurrentKeyChangeDictionary()
    {
        return m_currentKeyChangeDictionary;
    }

    public KeyChanges describeChanges(Class aClass, ClassDatabase cdb)
    {
        KeyChanges keyChanges = new KeyChanges();
        ChangeModel changeModel = createChangeModel();
        for (ChangeSet changeSet : changeModel.getChangeSets())
        {
            if (!keyChanges.m_added)
            {
                for (NewKey newKey : changeSet.getNewKeyChanges())
                {
                    Class c = cdb.getClassModelBySimpleName(newKey.getClazz()).getContainedClass();
                    if(aClass.isAssignableFrom(c))
                    {
                        keyChanges.m_added = true;
                        break;
                    }
                }
            }
            if (!keyChanges.m_changed)
            {
                for (PrimaryKeyChange primaryKeyChange : changeSet.getPrimaryKeyChanges())
                {
                    Class c = cdb.getClassModelBySimpleName(primaryKeyChange.getClazz()).getContainedClass();
                    if(aClass.isAssignableFrom(c))
                    {
                        keyChanges.m_changed = true;
                        break;
                    }
                }
            }
            if (!keyChanges.m_removed)
            {
                for (RemovedKey removedKey: changeSet.getRemovedKeyChanges())
                {
                    Class c = cdb.getClassModelBySimpleName(removedKey.getClazz()).getContainedClass();
                    if(aClass.isAssignableFrom(c))
                    {
                        keyChanges.m_removed = true;
                        break;
                    }
                }
            }
        }
        return keyChanges;
    }
}
