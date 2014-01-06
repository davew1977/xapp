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

public class RemovedNodeDiff implements Diff
{
    private String m_nodeClass;
    private String m_containerClass;
    private String m_containerKey;
    private String m_listProperty;
    private String m_key;


    public RemovedNodeDiff(String nodeClass, String containerClass, String containerKey, String listProperty, String key)
    {
        m_nodeClass = nodeClass;
        m_containerClass = containerClass;
        m_containerKey = containerKey;
        m_listProperty = listProperty;
        m_key = key;
    }

    public RemovedNodeDiff()
    {
    }

    public String createKey()
    {
        return m_containerClass+":"+m_containerKey+":"+m_listProperty;
    }

    public String getNodeClass()
    {
        return m_nodeClass;
    }

    public void setNodeClass(String nodeClass)
    {
        m_nodeClass = nodeClass;
    }

    public String getContainerClass()
    {
        return m_containerClass;
    }

    public void setContainerClass(String containerClass)
    {
        m_containerClass = containerClass;
    }

    public String getListProperty()
    {
        return m_listProperty;
    }

    public void setListProperty(String listProperty)
    {
        m_listProperty = listProperty;
    }

    public String getKey()
    {
        return m_key;
    }

    public void setKey(String key)
    {
        m_key = key;
    }

    public String getContainerKey()
    {
        return m_containerKey;
    }

    public void setContainerKey(String containerKey)
    {
        m_containerKey = containerKey;
    }
}
