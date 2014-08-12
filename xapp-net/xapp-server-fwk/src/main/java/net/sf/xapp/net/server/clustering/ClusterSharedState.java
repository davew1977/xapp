/*
 *
 * Date: 2010-sep-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.UserId;
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

    NodeId getNodeId(UserId userId);

    void addPlayerLocationMapping(UserId userId, NodeId nodeId);

    void removePlayerLocationMapping(UserId userId);

    PublicNodeState getNodeState(NodeId nodeId);

    void addNodeData(NodeData nodeData);
}
