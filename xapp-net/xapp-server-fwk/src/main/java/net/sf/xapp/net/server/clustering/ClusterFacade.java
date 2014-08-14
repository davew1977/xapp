/*
 *
 * Date: 2010-sep-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.common.types.PublicNodeState;
import net.sf.xapp.net.common.types.UserId;

import java.util.concurrent.BlockingQueue;

public interface ClusterFacade
{
    NodeId getThisNodesId();

    BlockingQueue<InMessage> getNodeQueue(NodeId nodeId);

    BlockingQueue<InMessage> getThisNodesQueue();

    void addEntityMapping(String key);

    void removeEntityMapping(String key);

    NodeId getNodeForEntity(String key);

    void postToTopic(String topicName, InMessage inMessage);

    void addTopicListener(String topicName, MessageHandler messageHandler);

    NodeId getNodeId(UserId userId);

    void addUserLocationMapping(UserId userId, NodeId nodeId);

    void removeUserLocationMapping(UserId userId);

    PublicNodeState getNodeState(NodeId nodeId);
}