/*
 *
 * Date: 2010-aug-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.server.clustering.ClusterFacade;
import ngpoker.common.framework.Message;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.connectionserver.listener.ConnectionListener;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import net.sf.xapp.net.server.connectionserver.messagesender.to.Broadcast;
import net.sf.xapp.net.server.connectionserver.messagesender.to.Post;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.nodeexitpoint.NodeExitPoint;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * stub added for a distributed environment
 */
public class MessageSenderProxy implements MessageSender, ConnectionListener
{
    private final Logger log = Logger.getLogger(getClass());
    private final Map<PlayerId, NodeId> connectionLocations;
    private final NodeExitPoint nodeExitPoint;
    private final ClusterFacade clusterFacade;

    public MessageSenderProxy(NodeExitPoint nodeExitPoint, ClusterFacade clusterFacade)
    {
        this.nodeExitPoint = nodeExitPoint;
        this.clusterFacade = clusterFacade;
        connectionLocations = new ConcurrentHashMap<PlayerId, NodeId>();
    }

    @Override
    /**
     * Sorts the broadcast recipients according to where they are connected, and dispatches to those nodes
     */
    public void broadcast(List<PlayerId> receivers, Message message)
    {
        Map<NodeId, List<PlayerId>> recipientMap = new HashMap<NodeId, List<PlayerId>>();
        for (PlayerId receiver : receivers)
        {
            NodeId nodeId = nodeId(receiver);
            if (nodeId!=null)
            {
                List<PlayerId> recipients = recipientMap.get(nodeId);
                if (recipients == null)
                {
                    recipients = new ArrayList<PlayerId>();
                    recipientMap.put(nodeId, recipients);
                }
                recipients.add(receiver);
            }
            else
            {
                log.debug("skipping broadcast to " + receiver + " who is not connected");
            }
        }
        for (Map.Entry<NodeId, List<PlayerId>> e : recipientMap.entrySet())
        {
            nodeExitPoint.sendAsyncMessage(e.getKey(), new Broadcast(e.getValue(), message));
        }
    }

    @Override
    public void post(PlayerId playerId, Message message)
    {
        NodeId nodeId = nodeId(playerId);
        if (nodeId!=null)
        {
            nodeExitPoint.sendAsyncMessage(nodeId, new Post(playerId, message));
        }
        else
        {
            log.debug("skipping post message to " + playerId + " who is not connected");
            log.debug(message);
        }
    }

    private NodeId nodeId(PlayerId playerId)
    {
        NodeId nodeId = connectionLocations.get(playerId);
        if(nodeId==null)
        {
            //lookup in cluster
            nodeId = clusterFacade.getNodeId(playerId);
            if(nodeId!=null)
            {
                connectionLocations.put(playerId, nodeId);
            }
        }
        return nodeId;
    }

    @Override
    public void playerConnected(PlayerId playerId, NodeId nodeId)
    {
        connectionLocations.put(playerId, nodeId);
    }

    @Override
    public void playerDisconnected(PlayerId playerId)
    {
        connectionLocations.remove(playerId);
    }
}
