package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.utils.XappException;

import java.util.*;

import static net.sf.xapp.objectmodelling.core.NamespacePath.*;
import static net.sf.xapp.utils.ReflectionUtils.*;

/**
 * additional data per instance
 */
public class ObjectMeta<T> implements Namespace{
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
        NamespaceFor namespaceFor = classModel !=null ? classModel.getNamespaceFor() : null;
        if (namespaceFor != null) {
            for (Class aClass : namespaceFor.value()) {
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
        Namespace namespace = getNamespace(aClass);
        return namespace.find(aClass, key);
    }

    public <E> ObjectMeta<E> find(Class<E> aClass, String path) {
        String[] p = path.split(NamespacePath.PATH_SEPARATOR, 2);
        if(p.length==1) {
            ObjectMeta<E> obj;
            if(aClass.isInterface()) {
                obj = allDirectDescendants(aClass).get(p[0]);
            } else {
                assert isNamespaceFor(aClass);
                obj = matchingMap(aClass).get(p[0]);
            }
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

    public Namespace getNamespace(ClassModel classModel) {
        return getNamespace(classModel.getContainedClass());
    }

    public Namespace getNamespace(Class aClass) {
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
        if(parent != null) {
            return parent.findMatchingMap(aClass);
        } else {
            assert isRoot();  //root is the implicit namespace for everything
            map = new LinkedHashMap<String, ObjectMeta>();
            lookupMap.put(mostGenericClass(aClass), map);
            return map;
        }
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

    public Map<String, ObjectMeta> getAll(final Class containedClass) {
        return getNamespace(containedClass).all(containedClass);
    }

    @Override
    public <E> Map<String, ObjectMeta<E>> all(Class<E> aClass) {
        Map<String, ObjectMeta<E>> result = new LinkedHashMap<String, ObjectMeta<E>>();
        if(aClass.isInterface()) { //search entire namespace, anything could implement it
            result.putAll(allDirectDescendants(aClass));
            for (Map.Entry<Class<?>, Set<ObjectMeta>> e : lookupSets.entrySet()) {
                for (ObjectMeta objectMeta : e.getValue()) {
                    if(objectMeta.isInstance(aClass)) {
                        result.put(fullPath(this, objectMeta), objectMeta);
                    }
                }
            }
        } else { //optimization: we only have to search the single matching map in the hierarchy
            Map<String, ObjectMeta> matchingMap = findMatchingMap(aClass);
            for (Map.Entry<String, ObjectMeta> e : matchingMap.entrySet()) {
                if(aClass.isAssignableFrom(e.getValue().getType())) {
                    result.put(e.getKey(), e.getValue());
                }
            }
            Set<ObjectMeta> objectMetas = findMatchingSet(aClass);
            for (ObjectMeta objectMeta : objectMetas) {
                if(aClass.isAssignableFrom(objectMeta.getType())) {
                    result.put(fullPath(this, objectMeta), objectMeta);
                }
            }
        }
        return result;
    }

    private <E> Map<String, ObjectMeta<E>> allDirectDescendants(Class<E> aClass) {
        Map<String, ObjectMeta<E>> result = new LinkedHashMap<String, ObjectMeta<E>>();
        for (Map.Entry<Class<?>, Map<String, ObjectMeta>> e : lookupMap.entrySet()) {
            Map<String, ObjectMeta> map = e.getValue();
            for (ObjectMeta objectMeta : map.values()) {
                if(objectMeta.isInstance(aClass)) {
                    result.put(objectMeta.getKey(), objectMeta);
                }
            }
        }
        return result;
    }

    private <E> boolean isInstance(Class<E> aClass) {
        return aClass.isInstance(instance);
    }

    @Override
    public String toString() {
        return key;
    }

    public Class<T> getType() {
        return classModel.getContainedClass();
    }
}
