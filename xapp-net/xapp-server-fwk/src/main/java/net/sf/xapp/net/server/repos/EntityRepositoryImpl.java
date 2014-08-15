/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.repos;

import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityRepositoryImpl implements EntityRepository
{
    private Map<Class, Map<String, Object>> entities;

    public EntityRepositoryImpl()
    {
        entities = new ConcurrentHashMap<Class, Map<String, Object>>();
    }

    @Override
    public <T> T find(Class<T> entityClass, String key)
    {
        Map<String, Object> map = entities.get(entityClass);
        return map != null ? (T) map.get(key) : null;
    }

    @Override
    public <T> void add(Class<T> entityClass, String key, T obj)
    {
        Map<String, Object> map = entityMapForClass(entityClass);
        if (map.containsKey(key))
        {
            throw new GenericException(ErrorCode.ENTITY_ALREADY_EXISTS, entityClass.getSimpleName() + " with key " + key + " already exists");
        }
        map.put(key, obj);
    }

    @Override
    public <T> T remove(Class<T> channelClass, String key)
    {
        return (T) entityMapForClass(channelClass).remove(key);
    }

    @Override
    public void removeAll(String key)
    {
        for (Map<String, Object> map : entities.values())
        {
            map.remove(key);
        }
    }

    @Override
    public int countEntities()
    {
        int count=0;
        for (Map<String, Object> map : entities.values())
        {
            count+=map.keySet().size();
        }
        return count;
    }

    @Override
    public int countEntitiesWithKey(String key)
    {
        int count = 0;
        for (Map<String, Object> map : entities.values())
        {
            if(map.containsKey(key))
            {
                count++;
            }
        }
        return count;
    }

    private Map<String, Object> entityMapForClass(Class entityClass)
    {
        Map<String, Object> map = entities.get(entityClass);
        if (map == null)
        {
            map = new ConcurrentHashMap<String, Object>();
            entities.put(entityClass, map);
        }
        return map;
    }
}
