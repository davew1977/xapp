/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.api.connectionlistener.ConnectionListener;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.framework.ThreadPoolInvoker;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

public class ChannelNodeInitializer
{
    public final ChannelConnectionListener channelConnectionListener;
    public final ExecutorService executorService;
    public final ClusterFacade clusterFacade;

    public ChannelNodeInitializer(ChannelConnectionListener channelConnectionListener,
                                  ExecutorService executorService,
                                  ClusterFacade clusterFacade)
    {
        this.channelConnectionListener = channelConnectionListener;
        this.executorService = executorService;
        this.clusterFacade = clusterFacade;
    }

    @PostConstruct
    public void init()
    {
        clusterFacade.addTopicListener("connectionListener",
                new ThreadPoolInvoker<ConnectionListener>(channelConnectionListener, executorService));
    }

}
