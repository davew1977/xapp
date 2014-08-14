/*
 *
 * Date: 2010-sep-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.api.nodeentrypoint.to.ReceiveAsyncMessage;
import net.sf.xapp.net.api.nodeentrypoint.to.ReceiveSyncRequest;
import net.sf.xapp.net.api.nodeentrypoint.to.ReceiveSyncResponse;
import net.sf.xapp.net.api.nodeexitpoint.NodeExitPoint;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.Response;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.NodeId;

public class NodeExitPointImpl implements NodeExitPoint
{
    private final ClusterFacade cluster;
    private final NodeInfo nodeInfo;

    public NodeExitPointImpl(ClusterFacade cluster, NodeInfo nodeInfo)
    {
        this.cluster = cluster;
        this.nodeInfo = nodeInfo;
    }

    @Override
    public void sendAsyncMessage(NodeId receiver, InMessage message)
    {
        cluster.getNodeQueue(receiver).add(new ReceiveAsyncMessage(nodeInfo.getMyNodeId(), message));
    }

    @Override
    public void sendSyncResponse(NodeId receiver, Response response, Long correlationId, ErrorCode errorCode)
    {
        cluster.getNodeQueue(receiver).add(new ReceiveSyncResponse(nodeInfo.getMyNodeId(), correlationId, response, errorCode));
    }

    @Override
    public void sendSyncRequest(NodeId receiver, InMessage request, Long correlationId)
    {
        cluster.getNodeQueue(receiver).add(new ReceiveSyncRequest(nodeInfo.getMyNodeId(), correlationId, request));
    }

    @Override
    public void sendToTopic(String topicName, InMessage message)
    {
        cluster.postToTopic(topicName,message);
    }
}
