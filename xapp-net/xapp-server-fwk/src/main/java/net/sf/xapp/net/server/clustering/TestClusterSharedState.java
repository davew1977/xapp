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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestClusterSharedState implements ClusterSharedState
{
    private Map<NodeId,BlockingQueue<InMessage>> nodeQMap = new HashMap<NodeId, BlockingQueue<InMessage>>();
    private Map<String, List<MessageHandler>> topicListeners;
    private Map<UserId, NodeId> playerLocations;
    private Map<String, NodeId> entityMappings;

    public TestClusterSharedState(int nodecount)
    {
        nodeQMap = new HashMap<NodeId, BlockingQueue<InMessage>>();
        for(int i=0;i<nodecount;i++)
        {
            nodeQMap.put(new NodeId(String.valueOf(i)), new LinkedBlockingQueue<InMessage>());
        }
        topicListeners = new HashMap<String, List<MessageHandler>>();
        playerLocations = new HashMap<UserId, NodeId>();
        entityMappings = new HashMap<String, NodeId>();
    }

    @Override
    public BlockingQueue<InMessage> getNodeQueue(NodeId nodeId)
    {
        return nodeQMap.get(nodeId);
    }

    @Override
    public void addEntityMapping(NodeId nodeId, String key)
    {
        entityMappings.put(key, nodeId);
    }

    @Override
    public void removeEntityMapping(String key)
    {
        entityMappings.remove(key);
    }

    @Override
    public void postToTopic(String topicName, InMessage inMessage)
    {
        for (MessageHandler messageHandler : getListeners(topicName))
        {
            messageHandler.handleMessage(inMessage);
        }
    }

    @Override
    public void addTopicListener(String topicName, MessageHandler messageHandler)
    {
        List<MessageHandler> listeners = getListeners(topicName);
        listeners.add(messageHandler);
    }

    private List<MessageHandler> getListeners(String topicName)
    {
        List<MessageHandler> listeners = topicListeners.get(topicName);
        if (listeners == null)
        {
            listeners = new ArrayList<MessageHandler>();
            topicListeners.put(topicName, listeners);
        }
        return listeners;
    }

    @Override
    public NodeId getNodeId(UserId userId)
    {
        return playerLocations.get(userId);
    }

    @Override
    public void addPlayerLocationMapping(UserId userId, NodeId nodeId)
    {
        playerLocations.put(userId, nodeId);
    }

    @Override
    public void removePlayerLocationMapping(UserId userId)
    {
        playerLocations.remove(userId);
    }

    @Override
    public NodeId getNodeForEntity(String key)
    {
        return entityMappings.get(key);
    }

    @Override
    public void addNodeData(NodeData nodeData)
    {

    }

    @Override
    public PublicNodeState getNodeState(NodeId nodeId)
    {
        return null;
    }
}
