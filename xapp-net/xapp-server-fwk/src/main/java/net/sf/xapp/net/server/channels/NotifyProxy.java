/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.UserId;

public class NotifyProxy<A> implements MessageHandler<A>
{
    private final CommChannel commChannel;
    private final UserId principal;

    public NotifyProxy(CommChannel commChannel)
    {
        this(commChannel, null);
    }

    public NotifyProxy(CommChannel commChannel, UserId principal)
    {
        this.commChannel = commChannel;
        this.principal = principal;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> atInMessage)
    {
        UserId pid = principal!=null ? principal : (UserId) atInMessage.principal();
        commChannel.send(pid, atInMessage);
        return null;
    }
}