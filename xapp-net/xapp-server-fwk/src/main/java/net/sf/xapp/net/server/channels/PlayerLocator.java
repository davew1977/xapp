/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.types.AppType;
import ngpoker.common.types.PlayerLocation;
import ngpoker.common.types.PlayerId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class PlayerLocator
{
    private Map<PlayerId, Set<String>> playersToLocations;
    private Map<String, PlayerLocation> locations;

    public PlayerLocator()
    {
        playersToLocations = new ConcurrentHashMap<PlayerId, Set<String>>();
        locations = new ConcurrentHashMap<String, PlayerLocation>();
    }

    public void registerLocation(PlayerLocation playerlocation)
    {
        locations.put(playerlocation.getKey(), playerlocation);
    }

    public void unregisterLocation(String channelKey)
    {
        //remove all links to this location
        for (Map.Entry<PlayerId, Set<String>> entry : playersToLocations.entrySet())
        {
            entry.getValue().remove(channelKey);
        }
        locations.remove(channelKey);
    }

    public void addMapping(PlayerId playerId, String appKey)
    {
        getAppKeys(playerId).add(appKey);
    }

    public void removeMapping(PlayerId playerId, String appKey)
    {
        getAppKeys(playerId).remove(appKey);
    }

    public List<PlayerLocation> getLocations(PlayerId playerId, AppType... include)
    {
        Set<String> appKeys = getAppKeys(playerId);
        ArrayList<PlayerLocation> channels = new ArrayList<PlayerLocation>();
        List<AppType> includeTypes = Arrays.asList(include);
        for (String channelKey : appKeys)
        {
            PlayerLocation location = locations.get(channelKey);
            if (includeTypes.isEmpty() || includeTypes.contains(location.getAppType()))
            {
                channels.add(location);
            }
        }
        return channels;
    }

    public Set<String> getAppKeys(PlayerId playerId)
    {
        Set<String> channels = playersToLocations.get(playerId);
        if (channels == null)
        {
            channels = new ConcurrentSkipListSet<String>();
            playersToLocations.put(playerId, channels);
        }
        return channels;
    }

    public int countLocations()
    {
        return locations.size();
    }
}
