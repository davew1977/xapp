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

import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Clipboard
{


    public boolean isCut() {
        return getAction() == Clipboard.Action.CUT;
    }

    public enum Action{CUT,COPY}
    private List<ObjectMeta> clipboardObjects = new ArrayList<ObjectMeta>();
    private Action m_action;

    public Clipboard()
    {
    }

    public boolean areAllInstanceOf(Class clazz)
    {
        for (ObjectMeta clipboardObject : clipboardObjects)
        {
            if(!clipboardObject.isA(clazz)) return false;
        }
        return true;
    }

    public boolean listContainsAny(Collection list)
    {
        for (ObjectMeta clipboardObject : clipboardObjects)
        {
            if(list.contains(clipboardObject.getInstance()))
            {
                return true;
            }
        }
        return false;
    }

    public void setClipboardObjects(List<ObjectMeta> clones)
    {
        clipboardObjects = clones;
    }

    public List<ObjectMeta> getClipboardObjects()
    {
        return clipboardObjects;
    }

    public void addClipboardObject(ObjectMeta clipboardObject)
    {
        clipboardObjects.add(clipboardObject);
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
        clipboardObjects.clear();
        m_action = action;
    }
}
