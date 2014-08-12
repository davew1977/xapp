/*
 *
 * Date: 2010-sep-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

public class TopicPublisherMessageHandler<A> implements MessageHandler<A>
{
    private final ClusterFacade clusterFacade;
    private final String topicName;

    public TopicPublisherMessageHandler(ClusterFacade clusterFacade, Class<A> apiType)
    {
        this.clusterFacade = clusterFacade;
        this.topicName = apiType.getSimpleName();
    }

    public TopicPublisherMessageHandler(ClusterFacade clusterFacade, String topicName)
    {
        this.clusterFacade = clusterFacade;
        this.topicName = topicName;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        clusterFacade.postToTopic(topicName, inMessage);
        return null;
    }
}
