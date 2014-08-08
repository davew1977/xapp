/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.memdb;

import net.sf.xapp.net.common.types.QueryData;
import net.sf.xapp.net.server.lobby.types.LobbyEntity;
import net.sf.xapp.net.server.framework.memdb.StorableType;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SubscriptionServiceImpl implements SubscriptionService
{
    private final Logger log = Logger.getLogger(getClass());
    private final String lobbyKey;
    private Database<LobbyEntity> db;
    private Map<QueryData, LiveQuery<LobbyEntity>> liveQueries;
    private Map<LiveQueryListener<LobbyEntity>, LiveQuery<LobbyEntity>> listeners;
    private LobbyEntityPropertyInspector propertyInspector;

    public SubscriptionServiceImpl(StorableType storableType, String lobbyKey)
    {
        this.lobbyKey = lobbyKey;
        this.propertyInspector = new LobbyEntityPropertyInspector();
        db = new CollaboratorDatabase<LobbyEntity>(this.propertyInspector, storableType.getPropertyNames());
        liveQueries = new HashMap<QueryData, LiveQuery<LobbyEntity>>();
        listeners = new HashMap<LiveQueryListener<LobbyEntity>, LiveQuery<LobbyEntity>>();
    }

    public void addItem(String key, LobbyEntity item)
    {
        db.store(key, item);
        for (LiveQuery<LobbyEntity> liveQuery: liveQueries.values())
        {
            liveQuery.itemAdded(item);
        }
    }

    public void removeItem(String key)
    {
        LobbyEntity item = db.remove(key);
        if (item!=null)
        {
            for (LiveQuery<LobbyEntity> liveQuery: liveQueries.values())
            {
                liveQuery.itemRemoved(item);
            }
        }
    }

    @Override
    public void itemChanged(String key, String propName, String value)
    {
        LobbyEntity item = db.findByKey(key);
        if (item!=null)
        {
            item.set(propName, value);
            for (LiveQuery<LobbyEntity> liveQuery : liveQueries.values())
            {
                liveQuery.itemChanged(item, propName, value);
            }
        }
        else
        {
            log.info(String.format("lobby %s received property change for null entity: key: %s prop: %s value: %s",
                    lobbyKey, key, propName, value));
        }
    }

    @Override
    public void itemChanged(String key, String propName, int index, String value, ListOp listOp)
    {
        LobbyEntity item = db.findByKey(key);
        if (item!=null)
        {
            item.set(propName, index, value, listOp);
            for (LiveQuery<LobbyEntity> liveQuery : liveQueries.values())
            {
                liveQuery.itemChanged(item, propName, index, value, listOp);
            }
        }
        else
        {
            log.info(String.format("lobby %s received property change for null entity: key: %s index: %s prop: %s value: %s",
                    lobbyKey, key, index, propName, value));
        }
    }

    @Override
    public int size()
    {
        return db.size();
    }

    @Override
    public Set<LobbyEntity> subscribe(QueryData query, LiveQueryListener<LobbyEntity> liveQueryListener)
    {
        LiveQuery<LobbyEntity> liveQuery = liveQueries.get(query);
        if(liveQuery==null)
        {
            liveQuery = new LiveQuery<LobbyEntity>(query, db.find(query), propertyInspector);
            liveQueries.put(query, liveQuery);
        }
        liveQuery.addListener(liveQueryListener);
        listeners.put(liveQueryListener, liveQuery);
        return liveQuery.getMatches();
    }

    @Override
    public void unsubscribe(LiveQueryListener<LobbyEntity> liveQueryListener)
    {
        LiveQuery<LobbyEntity> query = listeners.remove(liveQueryListener);
        query.removeListener(liveQueryListener);
    }
}
