/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.*;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.NodeData;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.common.types.PublicNodeState;
import net.sf.xapp.net.common.types.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class HazelcastClusterSharedState implements ClusterSharedState
{
    private HazelcastInstance hazelcastInstance;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public HazelcastClusterSharedState()
    {
        this(54327, 5701);
    }

    public HazelcastClusterSharedState(int multicastPort, int port)
    {
        log.info("starting new Hazelcast instance");
        Config config = new XmlConfigBuilder().build();
        config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(multicastPort);
        config.getNetworkConfig().setPort(port);
        config.getGroupConfig().setName("ngpoker");
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        log.info(hazelcastInstance.toString());
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
            public void onMessage(Message<InMessage> message) {
                messageHandler.handleMessage(message.getMessageObject());
            }
        });
    }

    @Override
    public NodeId getNodeId(UserId userId)
    {
        return (NodeId) hazelcastInstance.getMap("playerLocations").get(userId);
    }

    @Override
    public void addPlayerLocationMapping(UserId userId, NodeId nodeId)
    {
        hazelcastInstance.getMap("playerLocations").put(userId, nodeId);
    }

    @Override
    public void removePlayerLocationMapping(UserId userId)
    {
        hazelcastInstance.getMap("playerLocations").remove(userId);
    }
}
