/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.PlayerId;

public class NotifyProxy<A> implements MessageHandler<A>
{
    private final CommChannel commChannel;
    private final PlayerId principal;

    public NotifyProxy(CommChannel commChannel)
    {
        this(commChannel, null);
    }

    public NotifyProxy(CommChannel commChannel, PlayerId principal)
    {
        this.commChannel = commChannel;
        this.principal = principal;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> atInMessage)
    {
        PlayerId pid = principal!=null ? principal : (PlayerId) atInMessage.principal();
        commChannel.send(pid, atInMessage);
        return null;
    }
}