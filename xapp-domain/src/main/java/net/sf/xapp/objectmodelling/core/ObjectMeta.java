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
        Namespace namespace = classModel !=null ? classModel.getNamespace() : null;
        if (namespace != null) {
            for (Class aClass : namespace.value()) {
                lookupMap.put(aClass, new HashMap<String, ObjectMeta>());
                lookupSets.put(aClass, new HashSet<ObjectMeta>());
            }
        }
    }

    public void mapByKey(ObjectMeta obj) {
        assert isNamespaceFor(obj.getType());
        findMatchingMap(obj.getType()).put(obj.getKey(), obj);
    }

    public void remove(ObjectMeta obj) {
        assert isNamespaceFor(obj.getType());
        findMatchingMap(obj.getType()).remove(obj.getKey());
    }

    public void storeRef(ObjectMeta objectMeta) {
        assert isNamespaceFor(objectMeta.getType());
        findMatchingSet(objectMeta.getType()).add(objectMeta);
    }

    public void removeRef(ObjectMeta objectMeta) {
        assert isNamespaceFor(objectMeta.getType());
        findMatchingSet(objectMeta.getType()).remove(objectMeta);

    }

    public <E> E get(Class<E> aClass, String key) {
        return getObjMeta(aClass, key).getInstance();
    }

    public <E> ObjectMeta<E> getObjMeta(Class<E> aClass, String key) {
        ObjectMeta<?> namespace = getNamespace(aClass);
        return namespace.find(aClass, key);
    }

    private <E> ObjectMeta<E> find(Class<E> aClass, String path) {
        String[] p = path.split(NamespacePath.PATH_SEPARATOR, 2);
        if(p.length==1) {
            ObjectMeta<E> obj = findMatchingMap(aClass).get(p[0]);
            if (obj == null) {
                throw new XappException(String.format("%s %s not found, namespace is %s", aClass.getSimpleName(), path, instance));
            }
            return obj;
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

    /**
     * find the right place to put this object. It should have one home location, which in the simple case where
     * there are not explicit namespaces defined, will be in the root object meta
     */
    private Map<String, ObjectMeta> findMatchingMap(Class<?> aClass) {
        Map<String, ObjectMeta> map = matchingMap(aClass);
        if(map !=null) {
            return map;
        }
        if (isRoot()) { //root is the implicit namespace for everything
            map = new LinkedHashMap<String, ObjectMeta>();
            lookupMap.put(mostGenericClass(aClass), map);
            return map;
        }
        return parent.findMatchingMap(aClass);
    }

    private Map<String, ObjectMeta> matchingMap(Class<?> aClass) {
        for (Map.Entry<Class<?>, Map<String, ObjectMeta>> entry : lookupMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(aClass)) {
                return entry.getValue();
            }
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
            Set<ObjectMeta> set = new LinkedHashSet<ObjectMeta>();
            lookupSets.put(mostGenericClass(aClass), set);
            return set;
        }
        return new LinkedHashSet<ObjectMeta>();
    }


    /**
     * optionally initialize the instance with this object meta
     * if the object already has primary key then we need to update
     * parent object metas
     */
    public void initInstance() {
        String key = (String) get(classModel.getKeyProperty());
        if (key != null) {
           updateMetaHierarchy(null, key);
        }
        tryCall(instance, "setObjectMeta", this);
    }

    public boolean isNamespaceFor(Class aClass) {
        return isRoot() || matchingMap(aClass) != null;
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
            updateMetaHierarchy(oldVal, newVal);
        }
        return change;
    }

    public void updateMetaHierarchy(String oldKeyVal, String newKeyVal) {

        NamespacePath namespacePath = namespacePath(classModel.getContainedClass());
        //if last element is this object, then remove it
        if(namespacePath.getLast() == this) {
            namespacePath.removeLast();
        }
        ObjectMeta closestNamespace = namespacePath.removeLast();
        if (oldKeyVal != null) {
            closestNamespace.remove(this);
            if(newKeyVal == null) { //like deleting object
                for (ObjectMeta objectMeta : namespacePath) {
                    objectMeta.removeRef(this);
                }
            }
        }
        this.key = newKeyVal;
        if (newKeyVal != null) {
            closestNamespace.mapByKey(this);
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
            if(containedClass.isAssignableFrom(e.getValue().getType())) {
                result.put(e.getKey(), e.getValue().getInstance());
            }
        }
        Set<ObjectMeta> objectMetas = findMatchingSet(containedClass);
        for (ObjectMeta objectMeta : objectMetas) {
            if(containedClass.isAssignableFrom(objectMeta.getType())) {
                result.put(fullPath(this, objectMeta), objectMeta.getInstance());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return key;
    }

    public Class<T> getType() {
        return classModel.getContainedClass();
    }
}
