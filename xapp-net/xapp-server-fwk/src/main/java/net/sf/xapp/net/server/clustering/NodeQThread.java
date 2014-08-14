/*
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.api.nodeentrypoint.NodeEntryPoint;
import net.sf.xapp.net.common.framework.InMessage;

import java.util.concurrent.BlockingQueue;

/**
 * listens to the nodes queue and dispatches to the node's front end
 * One NodeQHandler Thread sits on each node waiting for new messages to appear on the node's
 * incoming message queue. Note these messages are the intra-node communication only.
 */
public class NodeQThread extends AbstractQThread
{
    private final NodeEntryPoint nodeEntryPoint;

    public NodeQThread(ClusterFacade cluster, boolean startThread, NodeEntryPoint nodeEntryPoint)
    {
        super(cluster, startThread);
        this.nodeEntryPoint = nodeEntryPoint;
    }

    /**
     * process a message that has been especially redirected to this node
     *
     * @param inMessage
     */
    public void processMessage(InMessage inMessage)
    {
        inMessage.visit(nodeEntryPoint);
    }

    @Override
    BlockingQueue<InMessage> getQueueToPoll()
    {
        return cluster.getThisNodesQueue();
    }

}