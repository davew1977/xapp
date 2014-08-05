/*
 *
 * Date: 2010-mar-05
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.net.client.io.HostInfo;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ClusterClient
{
    public static void main(String[] args)
    {
        if(args.length==0)
        {
            System.out.println("Usage: ClusterClient [<host:port> <host:port>](list) OR [<port> <port>](list)\n" +
                    "leaving out host defaults to localhost");
        }
        //launch against local host
        ClusterInfo clusterInfo = new ClusterInfo();
        for (String arg : args)
        {
            HostInfo n = HostInfo.parse(arg);
            clusterInfo.m_hostInfos.add(n);
        }
        startClient(clusterInfo);
    }

    public static void startClient(ClusterInfo clusterInfo)
    {

        final MultiNodeClientGUI<ScriptAdminClient> gui = createAdminClientGUI(clusterInfo, TestData.load());
        JFrame jf = SwingUtils.createFrame(gui);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

    }

    public static MultiNodeClientGUI<ScriptAdminClient> createAdminClientGUI(ClusterInfo clusterInfo, TestData data)
    {
        MultiNodeClientGUIListener<ScriptAdminClient> nodeClientGUIListener = new GUIListener();

        final MultiNodeClientGUI<ScriptAdminClient> gui = new MultiNodeClientGUI<ScriptAdminClient>();
        gui.setListener(nodeClientGUIListener);

        final List<ScriptAdminClient> adminClients = new ArrayList<ScriptAdminClient>();
        for (HostInfo hostInfo : clusterInfo.m_hostInfos)
        {
            ScriptAdminClient adminClient = new ScriptAdminClient(hostInfo, data);
            adminClients.add(adminClient);
        }
        gui.init(adminClients);
        for (int i = 0; i < clusterInfo.m_hostInfos.size(); i++) {
            HostInfo hostInfo = clusterInfo.m_hostInfos.get(i);
            ScriptAdminClient adminClient = adminClients.get(i);
            MinaServerProxy serverProxy = new MinaServerProxy(hostInfo);
            serverProxy.setClient(adminClient);
            adminClient.init(serverProxy, gui);

        }
        return gui;
    }

    private static class GUIListener implements MultiNodeClientGUIListener<ScriptAdminClient>
    {
        @Override
        public void newMessageTyped(ScriptAdminClient src, String message)
        {
            src.sendMessage(message);
        }
    }
}