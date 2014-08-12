/*
 *
 * Date: 2011-jan-14
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;

/**
 * A simple notifier, to allow beans send messages to players without bothering with a channel
 * @param <A>
 */
public class SimpleNotifier<A> implements MessageHandler<A>
{
    private final UserId principal;
    private final MessageSender messageSender;

    public SimpleNotifier(MessageSender messageSender)
    {
        this(messageSender, null);
    }
    
    public SimpleNotifier(MessageSender messageSender, UserId principal)
    {
        this.messageSender = messageSender;
        this.principal = principal;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> atInMessage)
    {
        UserId pid = principal!=null ? principal : (UserId) atInMessage.principal();
        messageSender.post(pid, atInMessage);
        return null;
    }
}
