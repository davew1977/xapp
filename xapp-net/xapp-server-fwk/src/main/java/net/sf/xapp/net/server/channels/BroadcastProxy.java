/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;

public class BroadcastProxy<A, B> implements MessageHandler<A>
{
    private CommChannel commChannel;

    public BroadcastProxy(CommChannel commChannel)
    {
        this.commChannel = commChannel;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> atInMessage)
    {
        commChannel.broadcast(atInMessage);
        return null;
    }
}
