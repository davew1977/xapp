/*
 *
 * Date: 2010-sep-09
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
    private Map<PlayerId, NodeId> playerLocations;
    private Map<String, NodeId> entityMappings;

    public TestClusterSharedState(int nodecount)
    {
        nodeQMap = new HashMap<NodeId, BlockingQueue<InMessage>>();
        for(int i=0;i<nodecount;i++)
        {
            nodeQMap.put(new NodeId(String.valueOf(i)), new LinkedBlockingQueue<InMessage>());
        }
        topicListeners = new HashMap<String, List<MessageHandler>>();
        playerLocations = new HashMap<PlayerId, NodeId>();
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
    public NodeId getNodeId(PlayerId playerId)
    {
        return playerLocations.get(playerId);
    }

    @Override
    public void addPlayerLocationMapping(PlayerId playerId, NodeId nodeId)
    {
        playerLocations.put(playerId, nodeId);
    }

    @Override
    public void removePlayerLocationMapping(PlayerId playerId)
    {
        playerLocations.remove(playerId);
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
