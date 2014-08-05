/*
 *
 * Date: 2010-mar-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;


import net.sf.xapp.net.client.io.HostInfo;
import ngpoker.common.framework.InMessage;

import java.util.ArrayList;
import java.util.List;

public class ClusterUtils
{

    /**
     * Connect to all the nodes specified in the cluster info
     * then run the script, script lines in the form [node-index]:[command]
     * @param clusterInfo
     * @param script
     */
    public static void runClusterScript(ClusterInfo clusterInfo, String script)
    {
        //connect to all nodes in cluster
        List<ScriptAdminClient> clients = new ArrayList<ScriptAdminClient>();
        TestData data = TestData.load();
        for (HostInfo hostInfo : clusterInfo.m_hostInfos)
        {
            ScriptAdminClient sac = new ScriptAdminClient(hostInfo, data)
            {
                @Override
                public Object handleMessage(InMessage message)
                {
                    System.out.println(this + " : " + message);
                    return null;
                }
            };
            MinaServerProxy minaServerProxy = new MinaServerProxy(hostInfo);
            minaServerProxy.setClient(sac);
            sac.init(minaServerProxy, null);
            clients.add(sac);
        }

        String[] lines = script.split("\n");
        for (String line : lines)
        {
            String[] args = line.split(":", 2);
            int nodeId = Integer.parseInt(args[0]);
            String command = args[1];
            clients.get(nodeId).sendMessage(command);
        }
    }
}