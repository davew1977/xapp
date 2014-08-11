/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.types.AppType;
import ngpoker.common.types.PlayerLocation;
import net.sf.xapp.net.common.types.UserId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class PlayerLocator
{
    private Map<UserId, Set<String>> playersToLocations;
    private Map<String, PlayerLocation> locations;

    public PlayerLocator()
    {
        playersToLocations = new ConcurrentHashMap<UserId, Set<String>>();
        locations = new ConcurrentHashMap<String, PlayerLocation>();
    }

    public void registerLocation(PlayerLocation playerlocation)
    {
        locations.put(playerlocation.getKey(), playerlocation);
    }

    public void unregisterLocation(String channelKey)
    {
        //remove all links to this location
        for (Map.Entry<UserId, Set<String>> entry : playersToLocations.entrySet())
        {
            entry.getValue().remove(channelKey);
        }
        locations.remove(channelKey);
    }

    public void addMapping(UserId userId, String appKey)
    {
        getAppKeys(userId).add(appKey);
    }

    public void removeMapping(UserId userId, String appKey)
    {
        getAppKeys(userId).remove(appKey);
    }

    public List<PlayerLocation> getLocations(UserId userId, AppType... include)
    {
        Set<String> appKeys = getAppKeys(userId);
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

    public Set<String> getAppKeys(UserId userId)
    {
        Set<String> channels = playersToLocations.get(userId);
        if (channels == null)
        {
            channels = new ConcurrentSkipListSet<String>();
            playersToLocations.put(userId, channels);
        }
        return channels;
    }

    public int countLocations()
    {
        return locations.size();
    }
}
