/*
 *
 * Date: 2010-sep-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.api.nodeentrypoint.NodeEntryPoint;
import net.sf.xapp.net.api.nodeexitpoint.NodeExitPoint;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.Response;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.NodeId;
import net.sf.xapp.net.server.clustering.syncreq.SyncStubProxy;

import java.util.concurrent.ExecutorService;

public class NodeEntryPointImpl implements NodeEntryPoint
{
    private final SyncStubProxy syncStubProxy;
    private final ExecutorService executorService;
    private final NodeExitPoint nodeExitPoint;
    private final BeanManager beanManager;
    private final NodeAsyncMessageHandler nodeAsyncMessageHandler;

    public NodeEntryPointImpl(SyncStubProxy syncStubProxy,
                              ExecutorService executorService,
                              NodeExitPoint nodeExitPoint,
                              BeanManager beanManager,
                              NodeAsyncMessageHandler nodeAsyncMessageHandler)
    {
        this.syncStubProxy = syncStubProxy;
        this.executorService = executorService;
        this.nodeExitPoint = nodeExitPoint;
        this.beanManager = beanManager;
        this.nodeAsyncMessageHandler = nodeAsyncMessageHandler;
    }

    @Override
    public void receiveAsyncMessage(NodeId sender, InMessage message)
    {
        nodeAsyncMessageHandler.handleMessage(message);
    }

    @Override
    public void receiveSyncResponse(NodeId sender, Long correlationId, Response response, ErrorCode errorCode)
    {
        syncStubProxy.responseReceived(correlationId, response, errorCode);
    }

    @Override
    public void receiveSyncRequest(final NodeId sender, final Long correlationId, final InMessage request)
    {
        //need to look up the bean
        final MessageHandler bean = beanManager.findBean(request.api());
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Response response = null;
                ErrorCode e = null;
                try
                {
                    response = (Response) bean.handleMessage(request);
                }
                catch (GenericException ge)
                {
                    e = ge.getErrorCode();
                }
                nodeExitPoint.sendSyncResponse(sender, response, correlationId, e);
            }
        });
    }
}
