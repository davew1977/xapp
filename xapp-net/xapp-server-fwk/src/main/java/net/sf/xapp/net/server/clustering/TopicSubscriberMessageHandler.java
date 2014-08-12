/*
 *
 * Date: 2010-sep-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

public class TopicSubscriberMessageHandler<A> implements MessageHandler<A>
{
    private final A delegate;

    public TopicSubscriberMessageHandler(ClusterFacade clusterFacade, Class<A> apiType, A delegate)
    {
        this.delegate = delegate;
        clusterFacade.addTopicListener(apiType.getSimpleName(), this);
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        return inMessage.visit(delegate);
    }
}