/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.memdb;

import ngpoker.common.types.ListOp;
import ngpoker.lobby.types.LobbyEntity;
import ngpoker.lobby.types.QueryData;

import java.util.Set;

public interface SubscriptionService
{


    /**
     * subscribe to changes in the set
     * @param liveQueryListener
     * @return
     */
    Set<LobbyEntity> subscribe(QueryData query, LiveQueryListener<LobbyEntity> liveQueryListener);
    void unsubscribe(LiveQueryListener<LobbyEntity> liveQueryListener);

    void addItem(String key, LobbyEntity item);
    void removeItem(String key);

    void itemChanged(String key, String propName, String value);
    void itemChanged(String key, String propName, int index, String value, ListOp listOp);

    /**
     * return the number of managed items
     * @return
     */
    int size();
}
