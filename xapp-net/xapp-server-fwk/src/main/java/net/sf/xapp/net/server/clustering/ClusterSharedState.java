/*
 *
 * Date: 2010-sep-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.PlayerId;
import ngpoker.infrastructure.types.NodeData;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.infrastructure.types.PublicNodeState;

import java.util.concurrent.BlockingQueue;

public interface ClusterSharedState
{
    BlockingQueue<InMessage> getNodeQueue(NodeId nodeId);

    void addEntityMapping(NodeId nodeId, String key);

    void removeEntityMapping(String key);

    NodeId getNodeForEntity(String key);

    void postToTopic(String topicName, InMessage inMessage);

    void addTopicListener(String topicName, MessageHandler messageHandler);

    NodeId getNodeId(PlayerId playerId);

    void addPlayerLocationMapping(PlayerId playerId, NodeId nodeId);

    void removePlayerLocationMapping(PlayerId playerId);

    PublicNodeState getNodeState(NodeId nodeId);

    void addNodeData(NodeData nodeData);
}
