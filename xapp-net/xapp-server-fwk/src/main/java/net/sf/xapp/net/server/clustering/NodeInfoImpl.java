/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import ngpoker.infrastructure.types.NodeId;

public class NodeInfoImpl implements NodeInfo {
    private final NodeId nodeId;
    private final int nodeIndex;
    private final String backupDir;

    public NodeInfoImpl(int nodeIndex, String backupDir) {
        this.nodeIndex = nodeIndex;
        this.backupDir = backupDir;
        this.nodeId = new NodeId(nodeIndex + "");
    }

    @Override
    public NodeId getMyNodeId() {
        return nodeId;
    }

    @Override
    public int getNodeIndex() {
        return nodeIndex;
    }

    @Override
    public String getNodeAlias() {
        return getMyNodeId().getValue();
    }

    @Override
    public String getBackupDir() {
        return backupDir;
    }
}
