/*
 *
 * Date: 2010-sep-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import ngpoker.common.types.ErrorCode;
import net.sf.xapp.net.server.clustering.syncreq.SyncStubProxy;
import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.Response;
import ngpoker.common.types.GenericException;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.nodeentrypoint.NodeEntryPoint;
import ngpoker.common.framework.MessageHandler;
import ngpoker.nodeexitpoint.NodeExitPoint;

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
