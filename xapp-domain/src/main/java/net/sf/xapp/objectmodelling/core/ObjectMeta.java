package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.filters.PropertyFilter;
import net.sf.xapp.utils.XappException;

import java.util.*;

import static net.sf.xapp.objectmodelling.core.NamespacePath.*;
import static net.sf.xapp.utils.ReflectionUtils.*;

/**
 * additional data per instance
 */
public class ObjectMeta<T> implements Namespace{
    private final ClassModel<T> classModel;
    private final T instance;
    private final Map<Class<?>, Map<String, ObjectMeta>> lookupMap = new HashMap<Class<?>, Map<String, ObjectMeta>>();
    private final Map<Class<?>, Set<ObjectMeta>> lookupSets = new HashMap<Class<?>, Set<ObjectMeta>>();
    private final Long id;
    private String key; //can change
    private ObjectLocation home; //the parent obj and the property where this is stored
    private Object attachment;//an arbitrary object to associate with this object meta
    private Map<ObjectLocation, Object> references = new HashMap<ObjectLocation, Object>();
    private List<PendingObjectReference> pendingRefsToSet = new ArrayList<PendingObjectReference>();

    public ObjectMeta(ClassModel classModel, T obj, ObjectLocation home, boolean updateModelHomeRef, int index) {
        final ClassDatabase cdb = classModel.getClassDatabase();
        this.classModel = classModel;
        this.instance = obj;
        NamespaceFor namespaceFor = classModel !=null ? classModel.getNamespaceFor() : null;
        if (namespaceFor != null) {
            for (Class aClass : namespaceFor.value()) {
                lookupMap.put(aClass, new HashMap<String, ObjectMeta>());
                lookupSets.put(aClass, new HashSet<ObjectMeta>());
            }
        }
        this.id = classModel.registerWithClassDatabase(this);
        key = (String) get(classModel.getKeyProperty());
        setHome(home, updateModelHomeRef, index);

        //add metas for children of this object
        List<Property> properties = classModel.getAllProperties(PropertyFilter.COMPLEX_NON_REFERENCE);
        for (Property property : properties) {
            property.eachValue(this, new PropertyValueIterator() {
                @Override
                public void exec(ObjectLocation objLocation, int index, Object val) {
                    cdb.findOrCreateObjMeta(objLocation, val);
                }
            });
        }
        //add references for all reference properties
        List<Property> refProps = classModel.getAllProperties(PropertyFilter.REFERENCE);
        for (Property refProp : refProps) {
            refProp.eachValue(this, new PropertyValueIterator() {
                @Override
                public void exec(ObjectLocation objectLocation, int index, Object value) {
                    ObjectMeta referee = objectLocation.getPropClassModel().find(value);
                    referee.createReference(objectLocation);
                }
            });
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
            Map<String, ObjectMeta> lookup;
            if(aClass.isInterface()) {
                lookup = allDirectDescendants(aClass); //todo optimize
            } else {
                assert isNamespaceFor(aClass);
                lookup = matchingMap(aClass);
            }
            obj = lookup != null ? lookup.get(p[0]) : null;
            if (obj == null) {
                //todo find the best match
                ClassModel cm = classModel.getClassDatabase().getClassModel(aClass);
                Vector<ObjectMeta> objMetas = cm.getAllInstancesInHierarchy();
                System.out.println(String.format("%s %s not found, namespace is %s\nperforming slow search instead", aClass.getSimpleName(), path, instance));
                for (ObjectMeta objMeta : objMetas) {
                    if(objMeta.getKey().equals(p[0])) {
                        return objMeta;
                    }
                }
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

    /**
     * return path of object, not including this one
     */
    public NamespacePath namespacePath(Class aClass) {
        NamespacePath path = new NamespacePath();
        ObjectMeta objectMeta = getParent();
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

    public ObjectLocation getHome() {
        return home;
    }

    public ObjectMeta getParent() {
        return !isRoot() ? home.getObj() : null; //may return null if root obj
    }

    public boolean isRoot() {
        return home == null;
    }

    public ObjectMeta root() {
        return isRoot() ? this : getParent().root();
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
        if(getParent()!= null) {
            return getParent().findMatchingMap(aClass);
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

    public boolean isNamespaceFor(Class aClass) {
        return isRoot() || matchingMap(aClass) != null;
    }

    public ClassModel<T> getClassModel() {
        return classModel;
    }

    public Object get(Property property) {
        return property != null ? property.get(getInstance()) : null;
    }

    public PropertyChange set(Property property, Object value) {
        RegularPropertyChange change = property.set(getInstance(), value);
        if (change != null) {
            if(property.isReference()) {
                if(value!=null) {
                    ObjectMeta objectMeta = property.getPropertyClassModel().find(value);
                    objectMeta.createReference(new ObjectLocation(this, property));
                }
                if(change.oldVal!=null) {
                    ObjectMeta objectMeta = property.getPropertyClassModel().find(change.oldVal);
                    objectMeta.removeReference(new ObjectLocation(this, property));
                }
            } else {
                if (property.isKey()) {
                    String oldVal = (String) change.oldVal;
                    assert Property.objEquals(oldVal, key);
                    String newVal = (String) change.newVal;
                    if(home != null) {
                        updateMetaHierarchy(newVal);
                        if(home.isMap()) {
                            home.keyChanged(oldVal, newVal);
                        }
                    }
                }
            }
        }
        return change;
    }

    public void updateMetaHierarchy(String newKeyVal) {
        assert home != null;
        NamespacePath namespacePath = namespacePath(classModel.getContainedClass());
        ObjectMeta closestNamespace = namespacePath.removeLast();
        if (key != null) {
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

    public Map<String, ObjectMeta> getAll(final Class containedClass) {
        return getNamespace(containedClass).all(containedClass);
    }

    @Override
    public  Map<String, ObjectMeta> all(Class aClass) {
        Map<String, ObjectMeta> result = new LinkedHashMap<String, ObjectMeta>();
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

    @Override
    public void addPendingRef(ObjectLocation targetLocation, String key) {
        pendingRefsToSet.add(new PendingObjectReference(targetLocation, key));
    }

    private Map<String, ObjectMeta> allDirectDescendants(Class aClass) {
        Map<String, ObjectMeta> result = new LinkedHashMap<String, ObjectMeta>();
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

    public String meta() {
        return String.format("id: %s, class: %s, key: %s, global key: %s", id, getType(), key, getGlobalKey());
    }

    public String getGlobalKey() {
        return getGlobalKey(NamespacePath.PATH_SEPARATOR);
    }

    public String getGlobalKey(String pathSeparator){
        return fullPath(root(), this, pathSeparator);
    }


    public Class<T> getType() {
        return classModel.getContainedClass();
    }

    public boolean isCloneable() {
        return instance instanceof Cloneable;
    }

    public Object get(String propertyName) {
        return get(classModel.getProperty(propertyName));
    }

    public void set(String propertyName, Object value) {
        Property property = classModel.getProperty(propertyName);
        if(value instanceof String) {
            property.setSpecial(this, (String) value);
        } else {
            set(property, value);
        }
    }

    public Long getId() {
        return id;
    }

    public Object getAttachment(ObjectLocation objectLocation) {
        return references.get(objectLocation);
    }
    public Object getAttachment() {
        return attachment;
    }

    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    public Collection<Object> dispose() {

        Collection<Object> attachments = attachments(); //store for returning later
        //delete all child objects
        List<ObjectMeta> children = allChildren();
        for (ObjectMeta child : children) {
            attachments.addAll(child.dispose());
        }

        ArrayList<ObjectLocation> refs = new ArrayList<ObjectLocation>(references.keySet());//copy to prevent concurrent modification
        for (ObjectLocation reference : refs) {
            reference.unset(this);
        }
        if (key != null) {
            updateMetaHierarchy(null);
        }


        classModel.dispose(this);
        if (home!=null) {
            home.unset(this);
        }
        return attachments;
    }

    private Collection<Object> attachments() {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Object o : references.values()) {
            if(o!=null) {
                result.add(o);
            }
        }
        return result;
    }

    private List<ObjectMeta> allChildren() {
        final List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        List<Property> properties = classModel.getAllProperties(PropertyFilter.COMPLEX_NON_REFERENCE);
        for (Property property : properties) {
            property.eachValue(this, new PropertyValueIterator() {
                @Override
                public void exec(ObjectLocation objectLocation, int index, Object value) {
                    result.add(objectLocation.getPropClassModel().find(value));
                }
            });
        }
        return result;
    }

    public PropertyChange setHome(ObjectLocation newLoc, boolean updateModel) {
        return setHome(newLoc, updateModel, -1);
    }

    public PropertyChange setHomeRef() {
        return setHomeRef(-1);
    }
    public PropertyChange setHomeRef(int index) {
        return this.home.set(this, index);
    }

    public PropertyChange setHome(ObjectLocation newLoc, boolean updateModel, int index) {
        ObjectLocation old = home;
        if (!Property.objEquals(old, newLoc)) {
            if (updateModel && old != null) {
                old.unset(this);
            }
            this.home = newLoc;
            if (updateModel && newLoc != null) {
                return setHomeRef(index);
            }
            updateMetaHierarchy(key);
        }
        return null;
    }

    public ClassDatabase getClassDatabase() {
        return classModel.getClassDatabase();
    }

    public Map<String, PropertyChange> update(List<PropertyUpdate> potentialUpdates) {
        return PropertyUpdate.execute(this, potentialUpdates);
    }

    public void createReference(ObjectLocation objectLocation) {
        references.put(objectLocation, null);
    }

    public PropertyChange createAndSetReference(ObjectLocation objectLocation) {
        references.put(objectLocation, null);
        return objectLocation.set(this);
    }

    public void attach(ObjectLocation objectLocation, Object attachment) {
        references.put(objectLocation, attachment);
    }

    public PropertyChange removeAndUnsetReference(ObjectLocation objectLocation) {
        references.remove(objectLocation);
        return objectLocation.unset(this);
    }

    public void removeReference(ObjectLocation objectLocation) {
        references.remove(objectLocation);
    }

    public int updateIndex(ObjectLocation location, int index) {
        //find "my" location
        ObjectLocation myLocation = resolve(location);
        return myLocation.adjustIndex(this, index);
    }

    private ObjectLocation resolve(ObjectLocation location) {
        if(!location.containsReferences()) {
            assert location.equals(home);
            return home;
        }
        for (ObjectLocation reference : references.keySet()) {
            if(reference.equals(location)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(String.format("object %s not in location %s", this, location));
    }

    public Object cloneInstance() {
        return getClassModel().createClone(instance);
    }

    public String getSimpleClassName() {
        return getClassModel().getContainedClass().getSimpleName();
    }

    public boolean isContainer() {
        return getClassModel().isContainer();
    }

    public ContainerProperty getContainerProperty() {
        return getClassModel().getContainerProperty();
    }

    public List<ContainerProperty> allContainerProps() {
        List<ContainerProperty> containerProperties = new ArrayList<ContainerProperty>(classModel.getMapProperties());
        List<ListProperty> listProperties = classModel.getListProperties();
        containerProperties.addAll(listProperties);
        return containerProperties;
    }

    public List<Property> getProperties() {
        return getClassModel().getProperties();
    }

    public Set<ClassModel> compatibleTypes() {
        Set<ClassModel> result = new HashSet<ClassModel>();
        if (!isRoot()) {
            result.add(classModel);
            ClassModel propertyClassModel = getHome().getProperty().getMainTypeClassModel();
            result.add(propertyClassModel);
            result.addAll(propertyClassModel.getValidImplementations());
        }
        return result;
    }

    public String printInfo() {
        StringBuilder sb = new StringBuilder(meta()).append("\n");
        if (isRoot()) {
            sb.append("THIS IS ROOT\n");
        } else {
            sb.append("Home: ").append(home.toString(this)).append("\n");
        }
        sb.append("\n");
        sb.append("References:\n");
        for (ObjectLocation objectLocation : references.keySet()) {
            sb.append("\t").append(objectLocation.toString(this)).append("\n");
        }
        sb.append("\n");
        sb.append("Lookup Maps:\n");
        for (Map.Entry<Class<?>, Map<String, ObjectMeta>> classMapEntry : lookupMap.entrySet()) {
            sb.append("\t").append(classMapEntry.getKey()).append("\n");
            sb.append("\t\t").append(classMapEntry.getValue()).append("\n");
        }

        sb.append("\n");
        sb.append("Lookup Sets:\n");
        for (Map.Entry<Class<?>, Set<ObjectMeta>> classSetEntry : lookupSets.entrySet()) {
            sb.append("\t").append(classSetEntry.getKey()).append("\n");
            sb.append("\t\t").append(classSetEntry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public boolean hasReferences() {
        return !references.isEmpty();
    }

    public int index() {
        return getHome().indexOf(this);
    }

    public <A> ObjectMeta<A> findAncestor(Class<A> type) {
        if(!isRoot()) {
            ObjectMeta parent = getParent();
            return parent.isInstance(type) ?  parent : parent.findAncestor(type);
        } else {
            return null;
        }
    }

    public void postInit() {
        flushPendingRefs();
        classModel.tryAndCallPostInit(this);
    }

    public void flushPendingRefs() {
        for (PendingObjectReference pendingObjectReference : pendingRefsToSet) {
            ObjectLocation targetLocation = pendingObjectReference.getTargetLocation();
            String key = pendingObjectReference.getKey();
            find(targetLocation.getPropertyClass(), key).createAndSetReference(targetLocation);
        }
        pendingRefsToSet.clear();
    }

    public String toXml() {
        return classModel.getClassDatabase().createMarshaller(classModel.getContainedClass()).toXMLString(instance);
    }
}
