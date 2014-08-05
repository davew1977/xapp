/*
 *
 * Date: 2010-jun-14
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework;

public interface Repository<K,V>
{
    V getAndLock(K key);
    void unlock(K key, V object);
}
