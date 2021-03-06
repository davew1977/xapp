/*
 *
 * Date: 2010-aug-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.api.connectionlistener.ConnectionListener;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.api.messagesender.to.Broadcast;
import net.sf.xapp.net.api.messagesender.to.Post;
import net.sf.xapp.net.api.nodeexitpoint.NodeExitPoint;
import net.sf.xapp.net.common.framework.Message;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<UserId, NodeId> connectionLocations;
    private final NodeExitPoint nodeExitPoint;
    private final ClusterFacade clusterFacade;

    public MessageSenderProxy(NodeExitPoint nodeExitPoint, ClusterFacade clusterFacade)
    {
        this.nodeExitPoint = nodeExitPoint;
        this.clusterFacade = clusterFacade;
        connectionLocations = new ConcurrentHashMap<UserId, NodeId>();
    }

    @Override
    /**
     * Sorts the broadcast recipients according to where they are connected, and dispatches to those nodes
     */
    public void broadcast(List<UserId> receivers, Message message)
    {
        Map<NodeId, List<UserId>> recipientMap = new HashMap<NodeId, List<UserId>>();
        for (UserId receiver : receivers)
        {
            NodeId nodeId = nodeId(receiver);
            if (nodeId!=null)
            {
                List<UserId> recipients = recipientMap.get(nodeId);
                if (recipients == null)
                {
                    recipients = new ArrayList<UserId>();
                    recipientMap.put(nodeId, recipients);
                }
                recipients.add(receiver);
            }
            else
            {
                log.debug("skipping broadcast to " + receiver + " who is not connected");
            }
        }
        for (Map.Entry<NodeId, List<UserId>> e : recipientMap.entrySet())
        {
            nodeExitPoint.sendAsyncMessage(e.getKey(), new Broadcast(e.getValue(), message));
        }
    }

    @Override
    public void post(UserId userId, Message message)
    {
        NodeId nodeId = nodeId(userId);
        if (nodeId!=null)
        {
            nodeExitPoint.sendAsyncMessage(nodeId, new Post(userId, message));
        }
        else
        {
            log.debug("skipping post message to " + userId + " who is not connected");
            log.debug(message.toString());
        }
    }

    private NodeId nodeId(UserId userId)
    {
        NodeId nodeId = connectionLocations.get(userId);
        if(nodeId==null)
        {
            //lookup in cluster
            nodeId = clusterFacade.getNodeId(userId);
            if(nodeId!=null)
            {
                connectionLocations.put(userId, nodeId);
            }
        }
        return nodeId;
    }

    @Override
    public void userConnected(UserId userId, NodeId nodeId)
    {
        connectionLocations.put(userId, nodeId);
    }

    @Override
    public void userDisconnected(UserId userId)
    {
        connectionLocations.remove(userId);
    }
}
