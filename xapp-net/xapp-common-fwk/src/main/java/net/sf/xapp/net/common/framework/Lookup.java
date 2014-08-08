package net.sf.xapp.net.common.framework;

import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.objectmodelling.api.ClassDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class Lookup {
    private Map<Class<? extends Entity>, Map<String, Entity>> entities;

    public Lookup()
    {
        entities = new HashMap<Class<? extends Entity>, Map<String, Entity>>();
    }

    public <T extends Entity> void add(Class<T> entityClass, String key, T obj)
    {
        Map<String, Entity> map = entityMapForClass(entityClass);
        if (map.containsKey(key))
        {
            throw new GenericException(ErrorCode.ENTITY_ALREADY_EXISTS, entityClass.getSimpleName() + " with key " + key + " already exists");
        }
        map.put(key, obj);
    }

    public <T extends Entity> T remove(Class<T> channelClass, String key)
    {
        return (T) entityMapForClass(channelClass).remove(key);
    }


    public <T> T lookup(Class<T> type, String key) {

        Map<String, ? extends Entity> map = entities.get(type);
        return map != null ? (T) map.get(key) : null;
    }


    private Map<String, Entity> entityMapForClass(Class<? extends Entity> entityClass)
    {
        Map<String, Entity> map = entities.get(entityClass);
        if (map == null)
        {
            map = new HashMap<String, Entity>();
            entities.put(entityClass, map);
        }
        return map;
    }

    public void registerIn(ClassDatabase cdb) {
        for (Map.Entry<Class<? extends Entity>, Map<String, Entity>> e : entities.entrySet()) {
            Class<? extends Entity> aClass = e.getKey();
            Map<String, Entity> map = e.getValue();
            for (Map.Entry<String, Entity> entityEntry : map.entrySet()) {
                cdb.getClassModel(aClass).find(entityEntry.getValue());
            }
        }
    }
}
