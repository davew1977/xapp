/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.repos;

public interface EntityRepository
{
    <T> T find(Class<T> entityClass, String key);
    <T> void add(Class<T> entityClass, String key, T obj);

    <T> T remove(Class<T> channelClass, String key);

    /**
     * Remove all entries with key
     * @param key
     */
    void removeAll(String key);

    int countEntities();

    int countEntitiesWithKey(String key);
}
