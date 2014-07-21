/*
 *
 * Date: 2010-sep-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

public class NullMessageHandler<A> implements MessageHandler<A>
{
    @Override
    public <T> T handleMessage(InMessage<A, T> abtInMessage)
    {
        return null;
    }
}
