package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Namespace;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.XappException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * additional data per instance
 */
public class ObjectMeta<T> {
    private final ClassModel classModel;
    private final ObjectMeta<?> parent;
    private final T instance;
    private final Map<Class, Map<String, Object>> lookupMap = new HashMap<Class, Map<String, Object>>();

    public ObjectMeta(ClassModel classModel, T obj, Namespace namespace, ObjectMeta<?> parent) {
        this.classModel = classModel;
        this.instance = obj;
        this.parent = parent;
        if (namespace != null) {
            for (Class aClass : namespace.value()) {
                lookupMap.put(aClass, new HashMap<String, Object>());
            }
        }
    }

    public void add(Class aClass, String key, Object obj) {
        findMatchingMap(aClass).put(key, obj);
    }

    public <E> E get(Class<E> aClass, String key) {
        E obj = findMatchingMap(aClass).get(key);
        if(obj == null) {
            throw new XappException(String.format("%s %s not found, namespace is %s", aClass.getSimpleName(), key, instance));
        }
        return obj;
    }

    public ObjectMeta<?> getNamespace(Class aClass) {
        return isNamespaceFor(aClass) ? this : getParent().getNamespace(aClass);
    }

    public T getInstance() {
        return instance;
    }

    public ObjectMeta<?> getParent() {
        return parent;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    private <E> Map<String, E> findMatchingMap(Class<E> aClass) {
        if(lookupMap.containsKey(aClass)) {
            return (Map<String, E>) lookupMap.get(aClass);
        }
        Map<String, Object> match = null;
        for (Map.Entry<Class, Map<String, Object>> entry : lookupMap.entrySet()) {
            if(entry.getKey().isAssignableFrom(aClass)) {
                assert match == null : this + " " + aClass;
                return (Map<String, E>) entry.getValue();
            }
        }
        if(isRoot()) {
            while(aClass.getSuperclass() != Object.class) {
                aClass = (Class<E>) aClass.getSuperclass();
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            lookupMap.put(aClass, map);
            return (Map<String, E>) map;
        }
        return null;
    }



    /**
     * optionally initialize the instance with this object meta
     */
    public void initInstance() {
        ReflectionUtils.tryCall(instance, "setObjectMeta", this);
    }

    public boolean isNamespaceFor(Class aClass) {
        return findMatchingMap(aClass)!=null;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Object getProp(Property property) {
        return property.get(getInstance());
    }

    public PropertyChangeTuple setProp(Property property, Object value) {
        return property.set(getInstance(), value);
    }


    public ObjectMeta globalObjMetaLookup(Object value) {
        return getClassModel().globalObjMetaLookup(value);
    }
}
