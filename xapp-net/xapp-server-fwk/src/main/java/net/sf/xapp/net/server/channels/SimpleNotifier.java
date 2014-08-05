/*
 *
 * Date: 2011-jan-14
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;

/**
 * A simple notifier, to allow beans send messages to players without bothering with a channel
 * @param <A>
 */
public class SimpleNotifier<A> implements MessageHandler<A>
{
    private final PlayerId principal;
    private final MessageSender messageSender;

    public SimpleNotifier(MessageSender messageSender)
    {
        this(messageSender, null);
    }
    
    public SimpleNotifier(MessageSender messageSender, PlayerId principal)
    {
        this.messageSender = messageSender;
        this.principal = principal;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> atInMessage)
    {
        PlayerId pid = principal!=null ? principal : (PlayerId) atInMessage.principal();
        messageSender.post(pid, atInMessage);
        return null;
    }
}
