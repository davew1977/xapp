/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.server.repos.EntityRepository;
import ngpoker.client.channel.Channel;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.connectionserver.listener.ConnectionListener;
import ngpoker.infrastructure.types.NodeId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * notifies the node's channels about connection events
 *
 */
public class ChannelConnectionListener implements ConnectionListener
{
    private final EntityRepository entityRepository;
    private final PlayerLocator playerLocator;

    public ChannelConnectionListener(EntityRepository entityRepository, PlayerLocator playerLocator)
    {
        this.entityRepository = entityRepository;
        this.playerLocator = playerLocator;
    }

    @Override
    public void playerConnected(PlayerId playerId, NodeId nodeId)
    {
        Collection<Channel> channels = channels(playerId);
        for (Channel channel : channels)
        {
            channel.playerConnected(playerId);
        }
    }

    @Override
    public void playerDisconnected(PlayerId playerId)
    {
        Collection<Channel> channels = channels(playerId);
        for (Channel channel : channels)
        {
            channel.playerDisconnected(playerId);
        }
    }

    private Collection<Channel> channels(PlayerId playerId)
    {
        Set<String> channelKeys = playerLocator.getAppKeys(playerId);
        ArrayList<Channel> result = new ArrayList<Channel>(channelKeys.size());
        for (String channelKey : channelKeys)
        {
            Channel channelEntity = entityRepository.find(Channel.class, channelKey);
            if (channelEntity!=null)
            {
                result.add(channelEntity);
            }
        }
        return result;
    }
}
