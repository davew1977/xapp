/*
 *
 * Date: 2010-sep-07
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering.syncreq;

import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.ServiceLookup;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import ngpoker.infrastructure.types.NodeId;

/**
 * Supports runtime lookup of services.
 *
 * Dynamically checks if a service is supported locally or requires a remote call
 */
public class ProxyChooser implements MessageHandler
{
    private MessageHandler local;
    private MessageHandler remote;
    private ServiceLookup serviceLookup;
    private NodeInfo nodeInfo;

    @Override
    public Object handleMessage(InMessage inMessage)
    {
        NodeId nodeId = serviceLookup.lookupService(inMessage.api());
        if(nodeInfo.getMyNodeId().equals(nodeId))
        {
            return local.handleMessage(inMessage);
        }
        else
        {
            return remote.handleMessage(inMessage);
        }
    }
}
