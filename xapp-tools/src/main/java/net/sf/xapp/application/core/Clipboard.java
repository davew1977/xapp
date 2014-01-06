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
package net.sf.xapp.application.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Clipboard
{


    public enum Action{CUT,COPY}
    private List<Object> m_clipboardObjects = new ArrayList<Object>();
    private Action m_action;

    public Clipboard()
    {
    }

    public boolean areAllInstanceOf(Class clazz)
    {
        for (Object clipboardObject : m_clipboardObjects)
        {
            if(!clazz.isInstance(clipboardObject)) return false;
        }
        return true;
    }

    public boolean listContainsAny(Collection list)
    {
        for (Object clipboardObject : m_clipboardObjects)
        {
            if(list.contains(clipboardObject))
            {
                return true;
            }
        }
        return false;
    }

    public void setClipboardObjects(List<Object> clones)
    {
        m_clipboardObjects = clones;
    }

    public List<Object> getClipboardObjects()
    {
        return m_clipboardObjects;
    }

    public void addClipboardObject(Object clipboardObject)
    {
        m_clipboardObjects.add(clipboardObject);
    }

    public Action getAction()
    {
        return m_action;
    }

    /**
     * called by cut and past commands to initialise the clip board
     * @param action
     */
    public void setAction(Action action)
    {
        m_clipboardObjects.clear();
        m_action = action;
    }
}
