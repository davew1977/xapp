/*
 *
 * Date: 2010-sep-03
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering.syncreq;

import net.sf.xapp.net.api.nodeexitpoint.NodeExitPoint;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.Message;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.server.clustering.ServiceLookup;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * a stub for a synchronous api. Blocks until a response is received
 *
 * @param <A>
 */
public class SyncStubProxy<A> implements MessageHandler<A>
{
    private final NodeExitPoint nodeExitPoint;
    private final Map<Long, Request> waitingRequests;
    private final AtomicLong correlationKeyGen = new AtomicLong(0);
    private final ServiceLookup serviceLookup;
    private final ScheduledExecutorService scheduledExecutorService;
    
    private static final long SYNC_REQ_TIMEOUT = 10000;

    public SyncStubProxy(NodeExitPoint nodeExitPoint,
                         ServiceLookup serviceLookup,
                         ScheduledExecutorService scheduledExecutorService)
    {
        this.nodeExitPoint = nodeExitPoint;
        this.serviceLookup = serviceLookup;
        this.scheduledExecutorService = scheduledExecutorService;
        waitingRequests = new ConcurrentHashMap<Long, Request>();
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        final Long correlationId = correlationKeyGen.incrementAndGet();
        Request<T> r = new Request<T>();
        r.countDownLatch = new CountDownLatch(1);
        waitingRequests.put(correlationId, r);
        //set up timeout
        r.timeoutFuture = scheduledExecutorService.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                Request request = waitingRequests.remove(correlationId);
                if (request != null)
                {
                    request.timedOut = true;
                    request.countDownLatch.countDown();
                }
            }
        }, SYNC_REQ_TIMEOUT, TimeUnit.MILLISECONDS);

        nodeExitPoint.sendSyncRequest(serviceLookup.lookupService(inMessage.api()), inMessage, correlationId);

        try
        {
            r.countDownLatch.await();
            if (r.timedOut)
            {
                throw new GenericException(ErrorCode.TIMEOUT,  "request timed out: " + inMessage.serialize());
            }
            if (r.errorCode != null)
            {
                throw new GenericException(r.errorCode);
            }

            return r.response;
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            waitingRequests.remove(correlationId);
        }
    }

    public void responseReceived(Long correlationId, Message response, ErrorCode errorCode)
    {
        Request request = waitingRequests.get(correlationId);
        if (request == null)
        {
            throw new RuntimeException("response received for timed out request. Not Good!" + response);
        }
        request.response = response;
        request.errorCode = errorCode;
        request.countDownLatch.countDown();
        request.timeoutFuture.cancel(false);
    }

    private static class Request<T>
    {
        CountDownLatch countDownLatch;
        T response;
        ErrorCode errorCode;
        boolean timedOut;
        ScheduledFuture timeoutFuture;
    }
}
