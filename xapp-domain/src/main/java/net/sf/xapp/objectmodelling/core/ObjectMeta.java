package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Namespace;
import net.sf.xapp.utils.XappException;

import java.util.*;

import static net.sf.xapp.objectmodelling.core.NamespacePath.*;
import static net.sf.xapp.utils.ReflectionUtils.*;

/**
 * additional data per instance
 */
public class ObjectMeta<T> {
    private final ClassModel classModel;
    private final ObjectMeta<?> parent;
    private final T instance;
    private final Map<Class<?>, Map<String, ObjectMeta>> lookupMap = new HashMap<Class<?>, Map<String, ObjectMeta>>();
    private final Map<Class<?>, Set<ObjectMeta>> lookupSets = new HashMap<Class<?>, Set<ObjectMeta>>();
    private String key;

    public ObjectMeta(ClassModel classModel, T obj, ObjectMeta<?> parent) {
        this.classModel = classModel;
        this.instance = obj;
        this.parent = parent;
        Namespace namespace = classModel.getNamespace();
        if (namespace != null) {
            for (Class aClass : namespace.value()) {
                lookupMap.put(aClass, new HashMap<String, ObjectMeta>());
                lookupSets.put(aClass, new HashSet<ObjectMeta>());
            }
        }
    }

    public void mapByKey(ClassModel classModel, String key, ObjectMeta obj) {
        assert isNamespaceFor(classModel);
        findMatchingMap_internal(classModel.getContainedClass()).put(key, obj);
    }

    public void remove(ClassModel classModel, String key) {
        assert isNamespaceFor(classModel);
        findMatchingMap_internal(classModel.getContainedClass()).remove(key);
    }

    public void storeRef(ObjectMeta objectMeta) {
        assert isNamespaceFor(objectMeta.getClassModel());
        findMatchingSet(objectMeta.classModel.getContainedClass()).add(objectMeta);
    }

    public void removeRef(ObjectMeta objectMeta) {
        assert isNamespaceFor(objectMeta.getClassModel());
        findMatchingSet(objectMeta.classModel.getContainedClass()).remove(objectMeta);

    }

    public <E> E get(Class<E> aClass, String key) {
        ObjectMeta<?> namespace = getNamespace(aClass);
        return namespace.find(aClass, key);
    }

    private <E> E find(Class<E> aClass, String path) {
        String[] p = path.split(NamespacePath.PATH_SEPARATOR, 2);
        if(p.length==1) {
            ObjectMeta<E> obj = findMatchingMap(aClass).get(p[0]);
            if (obj == null) {
                throw new XappException(String.format("%s %s not found, namespace is %s", aClass.getSimpleName(), path, instance));
            }
            return obj.getInstance();
        } else {
            //loop all lookup maps, path element must be unique across all classes otherwise more information would be
            //required in the path
            for (Map<String, ObjectMeta> objectMap : lookupMap.values()) {
                ObjectMeta<E> objectMeta = objectMap.get(p[0]);
                if(objectMeta != null) {
                    return objectMeta.find(aClass, p[1]);
                }
            }
            throw new XappException(String.format("%s %s not found, namespace is %s", aClass.getSimpleName(), path, instance));
        }
    }

    public ObjectMeta<?> getNamespace(ClassModel classModel) {
        return getNamespace(classModel.getContainedClass());
    }

    public ObjectMeta<?> getNamespace(Class aClass) {
        return isNamespaceFor(aClass) ? this : getParent().getNamespace(aClass);
    }

    public NamespacePath namespacePath(Class aClass) {
        NamespacePath path = new NamespacePath();
        ObjectMeta objectMeta = this;
        while(objectMeta != null) {
            if (objectMeta.isNamespaceFor(aClass)) {
                path.addFirst(objectMeta);
            }
            objectMeta = objectMeta.getParent();
        }
        return path;
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

    private Map<String, ObjectMeta> findMatchingMap(Class aClass) {

        Map<String, ObjectMeta> internalMatch = findMatchingMap_internal(aClass);
        if (internalMatch != null) {
            return internalMatch;
        }
        return parent.findMatchingMap(aClass);
    }

    private Map<String, ObjectMeta> findMatchingMap_internal(Class<?> aClass) {
        for (Map.Entry<Class<?>, Map<String, ObjectMeta>> entry : lookupMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(aClass)) {
                return entry.getValue();
            }
        }
        if (isRoot()) { //root is the implicit namespace for everything
            LinkedHashMap<String, ObjectMeta> map = new LinkedHashMap<String, ObjectMeta>();
            lookupMap.put(mostGenericClass(aClass), map);
            return map;
        }
        return null;
    }

    private Set<ObjectMeta> findMatchingSet(Class aClass) {
        for (Map.Entry<Class<?>, Set<ObjectMeta>> entry : lookupSets.entrySet()) {
            if (entry.getKey().isAssignableFrom(aClass)) {
                return entry.getValue();
            }
        }
        if (isRoot()) { //root is the implicit namespace for everything
            Set<ObjectMeta> map = new LinkedHashSet<ObjectMeta>();
            lookupSets.put(mostGenericClass(aClass), map);
            return map;
        }
        return null;
    }


    /**
     * optionally initialize the instance with this object meta
     * if the object already has primary key then we need to update
     * parent object metas
     */
    public void initInstance() {
        String key = (String) get(classModel.getKeyProperty());
        if (key != null) {
           initKey(null, key);
        }
        tryCall(instance, "setObjectMeta", this);
    }

    public boolean isNamespaceFor(ClassModel classModel) {
        return isNamespaceFor(classModel.getContainedClass());
    }

    public boolean isNamespaceFor(Class aClass) {
        return isRoot() || findMatchingMap_internal(aClass) != null;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public Object get(Property property) {
        return property != null ? property.get(getInstance()) : null;
    }

    public PropertyChange set(Property property, Object value) {
        PropertyChange change = property.set(getInstance(), value);
        if (property.isKey() && change != null) {
            String oldVal = (String) change.oldVal;
            String newVal = (String) change.newVal;
            initKey(oldVal, newVal);
        }
        return change;
    }

    private void initKey(String oldVal, String newVal) {
        this.key = newVal;

        NamespacePath namespacePath = namespacePath(classModel.getContainedClass());
        //if last element is this object, then remove it
        if(namespacePath.getLast() == this) {
            namespacePath.removeLast();
        }
        ObjectMeta closestNamespace = namespacePath.removeLast();
        if (oldVal != null) {
            closestNamespace.remove(classModel, oldVal);
            if(newVal == null) { //like deleting object
                for (ObjectMeta objectMeta : namespacePath) {
                    objectMeta.removeRef(this);
                }
            }
        }
        if (newVal != null) {
            closestNamespace.mapByKey(classModel, newVal, this);
            for (ObjectMeta objectMeta : namespacePath) {
                objectMeta.storeRef(this);
            }
        }
    }

    public NamespacePath getPath() {
        return namespacePath(classModel.getContainedClass());
    }
    public String getKey() {
        return key;
    }

    public ObjectMeta findOrCreateObjMeta(Object value) {
        return getClassModel().globalObjMetaLookup(this, value);
    }

    public Map<String, Object> getAll(final Class<?> containedClass) {
        Map<String, ObjectMeta> matchingMap = findMatchingMap(containedClass);
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, ObjectMeta> e : matchingMap.entrySet()) {
            if(containedClass.isAssignableFrom(e.getValue().getClassModel().getContainedClass())) {
                result.put(e.getKey(), e.getValue().getInstance());
            }
        }
        Set<ObjectMeta> objectMetas = findMatchingSet(containedClass);
        for (ObjectMeta objectMeta : objectMetas) {
            if(containedClass.isAssignableFrom(objectMeta.getClassModel().getContainedClass())) {
                result.put(fullPath(this, objectMeta), objectMeta.getInstance());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return key;
    }
}
