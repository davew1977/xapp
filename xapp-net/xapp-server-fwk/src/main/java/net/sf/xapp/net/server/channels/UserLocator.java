/*
 *
 * Date: 2010-sep-15
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class UserLocator
{
    private Map<UserId, Set<String>> playersToLocations;
    private Map<String, UserLocation> locations;

    public UserLocator()
    {
        playersToLocations = new ConcurrentHashMap<UserId, Set<String>>();
        locations = new ConcurrentHashMap<String, UserLocation>();
    }

    public void registerLocation(UserLocation userLocation)
    {
        locations.put(userLocation.getKey(), userLocation);
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

    public List<UserLocation> getLocations(UserId userId, AppType... include)
    {
        Set<String> appKeys = getAppKeys(userId);
        ArrayList<UserLocation> channels = new ArrayList<UserLocation>();
        List<AppType> includeTypes = Arrays.asList(include);
        for (String channelKey : appKeys)
        {
            UserLocation location = locations.get(channelKey);
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
