/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.NodeData;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.common.types.PublicNodeState;
import net.sf.xapp.net.common.types.UserId;

import java.util.concurrent.BlockingQueue;

public class ClusterFacadeImpl implements ClusterFacade
{
    private final ClusterSharedState clusterSharedState;
    private final NodeInfo nodeInfo;

    public ClusterFacadeImpl(ClusterSharedState clusterSharedState, NodeInfo nodeInfo)
    {
        this.clusterSharedState = clusterSharedState;
        this.nodeInfo = nodeInfo;
        clusterSharedState.addNodeData(new NodeData(nodeInfo.getMyNodeId(), PublicNodeState.RUNNING, "unknown"));
    }

    @Override
    public NodeId getThisNodesId()
    {
        return nodeInfo.getMyNodeId();
    }

    @Override
    public BlockingQueue<InMessage> getNodeQueue(NodeId nodeId)
    {
        return clusterSharedState.getNodeQueue(nodeId);
    }

    @Override
    public BlockingQueue<InMessage> getThisNodesQueue()
    {
        return clusterSharedState.getNodeQueue(nodeInfo.getMyNodeId());
    }

    @Override
    public void addEntityMapping(String key)
    {
        clusterSharedState.addEntityMapping(nodeInfo.getMyNodeId(), key);
    }

    @Override
    public void removeEntityMapping(String key)
    {
        clusterSharedState.removeEntityMapping(key);
    }

    @Override
    public NodeId getNodeForEntity(String key)
    {
        return clusterSharedState.getNodeForEntity(key);
    }

    @Override
    public void postToTopic(String topicName, InMessage inMessage)
    {
        clusterSharedState.postToTopic(topicName, inMessage);
    }

    @Override
    public void addTopicListener(String topicName, MessageHandler messageHandler)
    {
        clusterSharedState.addTopicListener(topicName, messageHandler);
    }

    @Override
    public NodeId getNodeId(UserId userId)
    {
        return clusterSharedState.getNodeId(userId);
    }

    @Override
    public void addUserLocationMapping(UserId userId, NodeId nodeId)
    {
        clusterSharedState.addPlayerLocationMapping(userId, nodeId);
    }

    @Override
    public void removeUserLocationMapping(UserId userId)
    {
        clusterSharedState.removePlayerLocationMapping(userId);
    }

    @Override
    public PublicNodeState getNodeState(NodeId nodeId)
    {
        return clusterSharedState.getNodeState(nodeId);
    }

}
