/*
 *
 * Date: 2010-jun-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.MessageTypeEnum;
import ngpoker.common.types.ErrorCode;
import ngpoker.common.types.GenericException;
import ngpoker.common.types.PlayerId;

import java.util.Map;

public class RequestTracker<A> implements MessageHandler<A>
{
    private Map<PlayerId, MessageTypeEnum> pendingRequests;
    private A requestApi;

    public RequestTracker(Map<PlayerId, MessageTypeEnum> pendingRequests)
    {
        this.pendingRequests = pendingRequests;
    }

    public void setRequestApi(A requestApi)
    {
        this.requestApi = requestApi;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        if(pendingRequests.containsKey((PlayerId) inMessage.principal()))
        {
            throw new GenericException(ErrorCode.PRINCIPAL_ALREADY_HAS_PENDING_REQUEST);
        }
        pendingRequests.put((PlayerId) inMessage.principal(), (MessageTypeEnum) inMessage.type());
        return inMessage.visit(requestApi);
    }
}
