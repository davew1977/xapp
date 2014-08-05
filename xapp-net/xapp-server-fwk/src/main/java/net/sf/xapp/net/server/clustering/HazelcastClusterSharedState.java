/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MessageListener;
import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.PlayerId;
import ngpoker.infrastructure.types.NodeData;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.infrastructure.types.PublicNodeState;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

public class HazelcastClusterSharedState implements ClusterSharedState
{
    private HazelcastInstance hazelcastInstance;
    private final Logger log = Logger.getLogger(getClass());

    public HazelcastClusterSharedState()
    {
        this(54327, 5701);
    }

    public HazelcastClusterSharedState(int multicastPort, int port)
    {
        log.info("starting new Hazelcast instance");
        Config config = new XmlConfigBuilder().build();
        config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(multicastPort);
        config.setPort(port);
        config.getGroupConfig().setName("ngpoker");
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        log.info(hazelcastInstance);
    }

    @Override
    public BlockingQueue<InMessage> getNodeQueue(NodeId nodeId)
    {
        return hazelcastInstance.getQueue(nodeId.getValue());
    }

    @Override
    public NodeId getNodeForEntity(String key)
    {
        return entityMap().get(key);
    }

    @Override
    public PublicNodeState getNodeState(NodeId nodeId)
    {
        NodeData nodeData = getNodeMap().get(nodeId);
        return nodeData.getNodeState();
    }

    @Override
    public void addNodeData(NodeData nodeData)
    {
        getNodeMap().put(nodeData.getNodeId(), nodeData);
    }

    private IMap<NodeId, NodeData> getNodeMap()
    {
        return hazelcastInstance.getMap("nodes");
    }

    @Override
    public void addEntityMapping(NodeId nodeId, String key)
    {
        entityMap().put(key, nodeId);
    }

    @Override
    public void removeEntityMapping(String key)
    {
        entityMap().remove(key);
    }

    private IMap<String, NodeId> entityMap()
    {
        return hazelcastInstance.getMap("entityMap");
    }

    @Override
    public void postToTopic(String topicName, InMessage inMessage)
    {
        hazelcastInstance.getTopic(topicName).publish(inMessage);
    }

    @Override
    public void addTopicListener(String topicName, final MessageHandler messageHandler)
    {
        hazelcastInstance.<InMessage>getTopic(topicName).addMessageListener(new MessageListener<InMessage>()
        {
            @Override
            public void onMessage(InMessage message)
            {
                messageHandler.handleMessage(message);
            }
        });
    }

    @Override
    public NodeId getNodeId(PlayerId playerId)
    {
        return (NodeId) hazelcastInstance.getMap("playerLocations").get(playerId);
    }

    @Override
    public void addPlayerLocationMapping(PlayerId playerId, NodeId nodeId)
    {
        hazelcastInstance.getMap("playerLocations").put(playerId, nodeId);
    }

    @Override
    public void removePlayerLocationMapping(PlayerId playerId)
    {
        hazelcastInstance.getMap("playerLocations").remove(playerId);
    }
}
