/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.net.client.io.HostInfo;

import java.util.ArrayList;
import java.util.List;

public class ClusterInfo
{
    @Key
    public String m_name;
    public List<HostInfo> m_hostInfos = new ArrayList<HostInfo>();

    @Override
    public String toString()
    {
        return m_name;
    }
}