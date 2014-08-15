/*
 *
 * Date: 2010-jun-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.types.UserId;

import java.util.Map;

public class RequestTracker<A> implements MessageHandler<A>
{
    private Map<UserId, MessageTypeEnum> pendingRequests;
    private A requestApi;

    public RequestTracker(Map<UserId, MessageTypeEnum> pendingRequests)
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
        if(pendingRequests.containsKey((UserId) inMessage.principal()))
        {
            throw new GenericException(ErrorCode.PRINCIPAL_ALREADY_HAS_PENDING_REQUEST);
        }
        pendingRequests.put((UserId) inMessage.principal(), (MessageTypeEnum) inMessage.type());
        return inMessage.visit(requestApi);
    }
}
