/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.api.nodeexitpoint.NodeExitPoint;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.common.types.PublicNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicEntryPoint implements MessageHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ClusterFacade cluster;
    private final NodeExitPoint nodeExitPoint;
    private final NodeAsyncMessageHandler nodeAsyncMessageHandler;
    private final ServiceLookup serviceLookup;

    public PublicEntryPoint(ClusterFacade cluster,
                            NodeExitPoint nodeExitPoint,
                            NodeAsyncMessageHandler nodeAsyncMessageHandler,
                            ServiceLookup serviceLookup)
    {
        this.cluster = cluster;
        this.serviceLookup = serviceLookup;
        this.nodeExitPoint = nodeExitPoint;
        this.nodeAsyncMessageHandler = nodeAsyncMessageHandler;
    }

    @Override
    public Void handleMessage(InMessage req)
    {
        if (log.isDebugEnabled())
        {
            log.debug("in " + req);
        }
        NodeId nodeId;
        if (req.entityKey() != null) //is it a targeted request?
        {
            nodeId = cluster.getNodeForEntity(req.entityKey());
            if(nodeId==null)
            {
                sendError(req, ErrorCode.ENTITY_DOES_NOT_EXIST_IN_CLUSTER);
                return null;
            }
        }
        else //or can it be handled by any node?
        {
            nodeId = serviceLookup.lookupService(req.api());
        }
        dispatchMessage(req, nodeId);
        return null;
    }

    private void dispatchMessage(InMessage req, NodeId nodeId)
    {
        if (nodeId.equals(cluster.getThisNodesId()))
        {
            handleLocally(req);
        }
        else
        {
            handleOther(req, nodeId);
        }
    }

    private void sendError(InMessage req, ErrorCode errorCode)
    {
        log.debug(req.type() + " " + errorCode);
    }

    private void handleOther(InMessage req, NodeId nodeId)
    {
        if (cluster.getNodeState(nodeId) == PublicNodeState.RUNNING)
        {
            forwardToNode(req, nodeId);
        }
        else //node is shutdown - e.g. perhaps it is being rolled
        {
            if(req.isPersistent())
            {
                forwardToNode(req, nodeId);
            }
            else
            {
                sendError(req, ErrorCode.NODE_IS_SHUTTING_DOWN);
            }
        }
    }

    private void forwardToNode(InMessage req, NodeId nodeId)
    {
        nodeExitPoint.sendAsyncMessage(nodeId, req);
        log.debug(String.format("forwarding %s to %s", req, nodeId));
    }

    private void handleLocally(InMessage req)
    {
        nodeAsyncMessageHandler.handleMessage(req);
    }
}
