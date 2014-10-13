package net.sf.xapp.objserver;

import net.sf.xapp.net.server.clustering.Node;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Main {

    public static void main(String[] args) {
        Node node = new Node(null, "/spring/basic-node.xml", "/spring/channels.xml", "/spring/admin-server.xml", "/spring/connection-server.xml");
    }
}
