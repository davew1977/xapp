/*
 *
 * Date: 2010-sep-07
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering.syncreq;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

public class LocalProxy<A> implements MessageHandler<A>
{
    private final A bean;

    public LocalProxy(A bean)
    {
        this.bean = bean;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        return inMessage.visit(bean);
    }
}
