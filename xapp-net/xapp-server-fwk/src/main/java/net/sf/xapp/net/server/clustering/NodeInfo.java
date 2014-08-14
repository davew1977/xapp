/*
 *
 * Date: 2010-sep-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.types.NodeId;

public interface NodeInfo {
    NodeId getMyNodeId();

    int getNodeIndex();

    String getNodeAlias();

    String getBackupDir();
}
