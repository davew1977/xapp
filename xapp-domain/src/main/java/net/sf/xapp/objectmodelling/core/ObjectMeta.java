package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Namespace;

import java.util.HashMap;
import java.util.Map;

/**
 * additional data per instance
 */
public class ObjectMeta<T> {
    private final T instance;
    private final Map<Class, Map<String, Object>> lookupMap = new HashMap<Class, Map<String, Object>>();

    public ObjectMeta(T obj, Namespace namespace) {
        this.instance = obj;
        if (namespace != null) {
            for (Class aClass : namespace.value()) {
                lookupMap.put(aClass, new HashMap<String, Object>());
            }
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

    public T getInstance() {
        return instance;
    }
}
