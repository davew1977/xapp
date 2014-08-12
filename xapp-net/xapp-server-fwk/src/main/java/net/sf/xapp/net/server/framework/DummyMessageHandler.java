/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

public class DummyMessageHandler<A> implements MessageHandler<A>
{
    @Override
    public <T> T handleMessage(InMessage<A, T> message)
    {
        return null;
    }
}
