package net.sf.xapp.objectmodelling.core;

import java.util.HashMap;
import java.util.Map;

/**
 * additional data per instance
 */
public class ObjectMeta {
    private Map<Class, Map<String, Object>> lookupMap = new HashMap<Class, Map<String, Object>>();

    public ObjectMeta(Class[] classes) {
        for (Class aClass : classes) {
            lookupMap.put(aClass, new HashMap<String, Object>());
        }
    }

    public void add(Class aClass, String key, Object obj) {
        lookupMap.get(aClass).put(key, obj);
    }

    public Object get(Class aClass, String key) {
        return lookupMap.get(aClass).get(key);
    }

    public boolean isNamespaceFor(Class aClass) {
        return lookupMap.containsKey(aClass);
    }
}
