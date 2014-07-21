/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.memdb;

import ngpoker.lobby.types.QueryData;

import java.util.Set;

/**
 * encapsulates some kind of searchable storage system
 * @param <T>
 */
public interface Database<T>
{
    void store(String key, T item);

    T remove(String key);

    Set<T> find(String encodedQuery);

    Set<T> find(QueryData query);

    T findByKey(String key);

    int size();
}
