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

public class ReferenceListDiff implements Diff
{
    private String m_containerClass;
    private String m_containerKey;
    private String m_listProperty;
    private List<String> m_addedNodes = new ArrayList<String>();
    private List<Integer> m_addedNodeIndexes = new ArrayList<Integer>();
    private List<String> m_removedNodes = new ArrayList<String>();


    public ReferenceListDiff(String containerClass, String containerKey, String listProperty, List<String> addedNodes, List<Integer> addedNodeIndexes, List<String> removedNodes)
    {
        m_containerClass = containerClass;
        m_containerKey = containerKey;
        m_listProperty = listProperty;
        m_addedNodes = addedNodes;
        m_addedNodeIndexes = addedNodeIndexes;
        m_removedNodes = removedNodes;
    }

    public ReferenceListDiff()
    {
    }

    public String createKey()
    {
        return m_containerClass+":"+m_containerKey+":"+m_listProperty;
    }

    public String getContainerClass()
    {
        return m_containerClass;
    }

    public void setContainerClass(String containerClass)
    {
        m_containerClass = containerClass;
    }

    public String getContainerKey()
    {
        return m_containerKey;
    }

    public void setContainerKey(String containerKey)
    {
        m_containerKey = containerKey;
    }

    public String getListProperty()
    {
        return m_listProperty;
    }

    public void setListProperty(String listProperty)
    {
        m_listProperty = listProperty;
    }

    @ListType(String.class)
    public List<String> getAddedNodes()
    {
        return m_addedNodes;
    }

    public void setAddedNodes(List<String> addedNodes)
    {
        m_addedNodes = addedNodes;
    }

    @ListType(Integer.class)
    public List<Integer> getAddedNodeIndexes()
    {
        return m_addedNodeIndexes;
    }

    public void setAddedNodeIndexes(List<Integer> addedNodeIndexes)
    {
        m_addedNodeIndexes = addedNodeIndexes;
    }

    @ListType(String.class)
    public List<String> getRemovedNodes()
    {
        return m_removedNodes;
    }

    public void setRemovedNodes(List<String> removedNodes)
    {
        m_removedNodes = removedNodes;
    }
}
