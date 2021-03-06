/*
 *
 * Date: 2010-jun-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.MessageTypeEnum;

import java.util.Map;

public class ResponseTracker<A> implements MessageHandler<A>
{
    private Map<String, MessageTypeEnum> pendingRequests;
    private A responseApi;

    public ResponseTracker(Map<String, MessageTypeEnum> pendingRequests)
    {
        this.pendingRequests = pendingRequests;
    }

    public void setResponseApi(A responseApi)
    {
        this.responseApi = responseApi;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> response)
    {
        pendingRequests.remove(response.principal());
        return response.visit(responseApi);
    }
}
