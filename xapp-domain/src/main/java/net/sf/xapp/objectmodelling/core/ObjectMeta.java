package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Namespace;
import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.XappException;

import java.util.*;

/**
 * additional data per instance
 */
public class ObjectMeta<T> {
    private final ClassModel classModel;
    private final ObjectMeta<?> parent;
    private final T instance;
    private final Map<Class, Map<String, Object>> lookupMap = new HashMap<Class, Map<String, Object>>();

    public ObjectMeta(ClassModel classModel, T obj, ObjectMeta<?> parent) {
        this.classModel = classModel;
        this.instance = obj;
        this.parent = parent;
        Namespace namespace = classModel.getNamespace();
        if (namespace != null) {
            for (Class aClass : namespace.value()) {
                lookupMap.put(aClass, new HashMap<String, Object>());
            }
        }
    }

    public void add(ClassModel classModel, String key, Object obj) {
        add(classModel.getContainedClass(), key, obj);
    }
    public void add(Class aClass, String key, Object obj) {
        findMatchingMap(aClass).put(key, obj);
    }
    public void remove(ClassModel aClass, String key) {
        findMatchingMap(aClass.getContainedClass()).remove(key);
    }

    public <E> E get(Class<E> aClass, String key) {
        E obj = findMatchingMap(aClass).get(key);
        if(obj == null) {
            throw new XappException(String.format("%s %s not found, namespace is %s", aClass.getSimpleName(), key, instance));
        }
        return obj;
    }

    public ObjectMeta<?> getNamespace(ClassModel classModel) {
        return getNamespace(classModel.getContainedClass());
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

        Map<String, E> internalMatch = findMatchingMap_internal(aClass);
        if (internalMatch != null) {
            return internalMatch;
        }
        if(isRoot()) {
            while(aClass.getSuperclass() != Object.class) {
                aClass = (Class<E>) aClass.getSuperclass();
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            lookupMap.put(aClass, map);
            return (Map<String, E>) map;
        }
        return parent.findMatchingMap(aClass);
    }

    private <E> Map<String, E> findMatchingMap_internal(Class<E> aClass) {
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
        return null;
    }


    /**
     * optionally initialize the instance with this object meta
     */
    public void initInstance() {
        ReflectionUtils.tryCall(instance, "setObjectMeta", this);
    }

    public boolean isNamespaceFor(Class aClass) {
        return isRoot() || findMatchingMap_internal(aClass)!=null;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Object get(Property property) {
        return property.get(getInstance());
    }

    public PropertyChange set(Property property, Object value) {
        PropertyChange change = property.set(getInstance(), value);
        if(property.isKey() && change != null) {
            if(change.oldVal != null) {
                remove(classModel, (String) change.oldVal);
            }
            if(change.newVal != null) {
                add(classModel, (String) change.newVal, instance);
            }
        }
        return change;
    }


    public ObjectMeta findOrCreateObjMeta(Object value) {
        return getClassModel().globalObjMetaLookup(this, value);
    }

    public <E> Collection<E> getAll(final Class<E> containedClass) {
        return CollectionsUtils.filter(findMatchingMap(containedClass).values(), new Filter<E>() {
            @Override
            public boolean matches(E e) {
                return containedClass.isAssignableFrom(e.getClass());
            }
        });
    }
}
