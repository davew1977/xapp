/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.application.BoundObjectType;
import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.XMLMapping;
import net.sf.xapp.annotations.objectmodelling.Children;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.TrackKeyChanges;
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.Rights;
import net.sf.xapp.objectmodelling.difftracking.*;
import net.sf.xapp.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.String.format;

/**
 * Class Model wraps a {@link Class} and also encapsulates the extra djwastor meta data.
 * <p/>
 * It also stores all the instances of that class that have been created via the framework (through unmarshalling
 * or through an application gui).
 * <p/>
 * It is also responsible for the creation process of these objects. When creating managed objects the ClassModel
 * tries to "inject" through reflection an instance of itself.
 */
public class ClassModel<T> {
    private static final Logger log = LoggerFactory.getLogger(ClassModel.class);
    private final List<ContainerProperty> mapProperties;
    private Class<T> m_class;
    private ClassDatabase m_classDatabase;
    private List<Property> properties;
    private Map<String, Property> m_propertyMap;
    private Map<String, Property> m_propertyMapByXMLMapping;
    private List<ListProperty> listProperties;
    private ContainerProperty childrenProperty;
    private Map<String, ClassModel> validImplementations;
    private int m_nextId;
    private BoundObjectType boundObjectType;
    private EditorWidget editorWidget;
    private Property keyProperty;
    private List<ObjectMeta<T>> instances;
    private Method postInitMethod;
    private Method preInitMethod;
    private ContainerProperty m_containerListProperty;
    private Set<Rights> m_restrictedRights;
    private TrackKeyChanges m_trackKeyChanges;
    private Class[] namespaceFor;

    public final String NESTED_CDATA_START = "]ATADC]!>";
    public final String NESTED_CDATA_END = ">]]";
    public final String CDATA_START = "<![CDATA[";
    public final String CDATA_END = "]]>";

    public ClassModel(ClassDatabase classDatabase,
                      Class aClass,
                      ClassModelFactory.InspectionTuple inspectionTuple,
                      List<ClassModel> validImplementations,
                      EditorWidget editorWidget,
                      BoundObjectType boundObjectType,
                      Property keyProperty,
                      String containerListProp) {
        m_class = aClass;

        postInitMethod = inspectionTuple.postInitMethod;
        preInitMethod = inspectionTuple.preInitMethod;

        m_classDatabase = classDatabase;
        properties = inspectionTuple.properties;
        listProperties = inspectionTuple.listProperties;
        mapProperties = inspectionTuple.mapProperties;
        m_propertyMap = new HashMap<String, Property>();
        m_propertyMapByXMLMapping = new HashMap<String, Property>();
        addProperties(properties);
        addProperties(listProperties);
        addProperties(mapProperties);
        instances = new ArrayList<ObjectMeta<T>>();
        this.boundObjectType = boundObjectType;
        this.editorWidget = editorWidget;
        this.validImplementations = new LinkedHashMap<>();
        if (validImplementations != null) {
            addValidImplementations(validImplementations);
        }
        this.keyProperty = keyProperty;
        if (this.keyProperty != null) {
            this.keyProperty.addChangeListener(new PrimaryKeyChangedListener());
        }
        if (containerListProp != null) {
            m_containerListProperty = (ContainerProperty) getProperty(containerListProp);
        }

        m_restrictedRights = new HashSet<Rights>();

        m_trackKeyChanges = (TrackKeyChanges) ClassUtils.getAnnotationInHeirarchy(TrackKeyChanges.class, m_class);
        if (m_trackKeyChanges != null && !hasKey()) {
            throw new XappException("class " + getSimpleName() + " is annotated with @TrackKeyChanges but has no primary key");
        }
        NamespaceFor nf = m_class.getAnnotation(NamespaceFor.class);
        namespaceFor = nf != null ? nf.value() : null;
    }

    public Map<String, ClassModel> getValidImplementations() {
        return validImplementations;
    }

    private static Collection<ClassModel> expand(Collection<ClassModel> validImplementations) {

        List<ClassModel> result = new ArrayList<ClassModel>();
        for (ClassModel subClass : validImplementations) {
            if(!subClass.getValidImplementations().isEmpty()) {
                result.addAll(expand(subClass.getValidImplementations().values()));
            } else {
                result.add(subClass);
            }
        }
        return result;
    }

    public Class[] getNamespaceFor() {
        if(namespaceFor != null) {
            return namespaceFor;
        }
        Class<? super T> superclass = m_class.getSuperclass();
        if(!superclass.equals(Object.class)) {
            return getClassDatabase().getClassModel(superclass).getNamespaceFor();
        }
        return new Class[]{};
    }

    public void setNamespaceFor(Class... namespaceFor) {
        this.namespaceFor = namespaceFor;
    }

    private void addProperties(List<? extends Property> properties) {
        for (Property property : properties) {
            String propNameLC = property.getName().toLowerCase();
            m_propertyMap.put(propNameLC, property);
            if (property.getXMLMapping() != null) {
                m_propertyMapByXMLMapping.put(property.getXMLMapping(), property);
            }
        }
    }

    public void addValidImplementations(Class... classes) {
        List<ClassModel> classModels = new ArrayList<>();
        for (Class aClass : classes) {
            classModels.add(m_classDatabase.getClassModel(aClass));
        }
        addValidImplementations(classModels);
    }
    public void addValidImplementations(List<ClassModel> classModels) {
        Collection<ClassModel> expanded = expand(classModels);
        for (ClassModel validImpl : expanded) {
            validImplementations.put(validImpl.getSimpleName(), validImpl);
        }
    }

    public void setContainerListProperty(String propertyName) {
        m_containerListProperty = (ContainerProperty) getProperty(propertyName);
    }

    public BoundObjectType getBoundObjectType() {
        return boundObjectType;
    }

    public EditorWidget getEditorWidget() {
        if(editorWidget != null) {
            return editorWidget;
        } else {
            ClassModel parent = getParent();
            if(parent != null) {
                return parent.getEditorWidget();
            }
        }
        return null;
    }

    public ClassModel getParent() {
        Class<? super T> superclass = m_class.getSuperclass();
        if(superclass != null && !superclass.equals(Object.class)) {
            return m_classDatabase.getClassModel(superclass);
        }
        return null;
    }

    public Class<T> getContainedClass() {
        return m_class;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Property> getNonTransientProperties() {
        List<Property> list = new ArrayList<Property>();
        for (Property property : properties) {
            if (!property.isTransient()) {
                list.add(property);
            }
        }
        for (ListProperty listProperty : listProperties) {
            if (listProperty.hasSpecialBoundComponent()) {
                list.add(listProperty);
            }
        }
        for (ContainerProperty mapProperty : mapProperties) {
            if (mapProperty.hasSpecialBoundComponent()) {
                list.add(mapProperty);
            }
        }
        return list;
    }

    public List<Property> getVisibleProperties() {
        List<Property> props = getNonTransientProperties();
        props.addAll(getNonTransientPrimitiveLists());
        ArrayList<Property> results = new ArrayList<Property>();
        for (Property prop : props) {
            if (prop.isVisibilityRestricted()) {
                results.add(prop);
            }
        }
        return results;
    }

    public List<ContainerProperty> getNonTransientPrimitiveLists() {
        List<ContainerProperty> list = new ArrayList<ContainerProperty>();
        for (ListProperty property : listProperties) {
            if (!property.isTransient() && !property.hasSpecialBoundComponent() && (
                    property.getContainedType() == String.class ||
                            property.getContainedType() == Integer.class ||
                            property.getContainedType() == Long.class ||
                            property.getContainedType().isEnum())) {
                list.add(property);
            }
        }
        for (ContainerProperty property : mapProperties) {
            if (!property.isTransient() && !property.hasSpecialBoundComponent() && (
                    property.isStringSerializable())) {
                list.add(property);
            }
        }
        return list;
    }

    public List<ListProperty> getListProperties() {
        return listProperties;
    }

    public List<ContainerProperty> getMapProperties() {
        return mapProperties;
    }

    public String toString() {
        return getSimpleName();
    }

    public String getSimpleName() {
        return m_class.getSimpleName();
    }

    public void clearValidImplementations() {
        validImplementations.clear();
    }

    public String getName(Object target) {
        Property property = m_propertyMap.get("name");
        if (property != null) {
            Object o = property.get(target);
            return o != null ? String.valueOf(o) : target.toString();
        } else {
            return target.toString();
        }
    }

    public boolean isAbstract() {
        return !validImplementations.isEmpty();
    }

    public int getNoOfProperties() {
        return listProperties.size() + properties.size();
    }

    /**
     * Create a new instance of the class.
     */
    public synchronized ObjectMeta<T> newInstance(ObjectLocation objectLocation, boolean updateModelHomeRef, boolean unmarshalling) {
        try {
            T obj = m_class.newInstance();
            return createObjMeta(objectLocation, obj, updateModelHomeRef, unmarshalling);
        } catch (Exception e) {
            System.out.println("cannot create instance of " + m_class);
            throw new XappException(e.getMessage(), e);
        }
    }

    /**
     * adds a previously unknown object to the model
     */
    public ObjectMeta<T> createObjMeta(ObjectLocation objectLocation, T obj, boolean updateModelHomeRef, boolean unmarshalling) {
        Long id = null;
        if(getClassDatabase().isMaster()) {
            id = getClassDatabase().nextId();
        }
        //TODO call a method on object notifying it is about to be added to the class DB
        ObjectMeta<T> objectMeta = new ObjectMeta<T>(this, obj, objectLocation, updateModelHomeRef, id, unmarshalling);
        if(hasPreInit()) {
            tryAndInvoke(obj, preInitMethod, objectMeta);
        }
        instances.add(objectMeta);
        if(!unmarshalling) {
            tryAndCallPostInit(objectMeta);
        }
        return objectMeta;
    }

    public Object getKey(T obj) {
        return getKeyProperty().get(obj);
    }

    public void dispose(T instance) {
        find(instance).dispose();
    }
    public void dispose(ObjectMeta objectMeta) {
        instances.remove(objectMeta);
        m_classDatabase.removeInstance(objectMeta);
        if (keyProperty != null) {
            Object oldKeyVal = objectMeta.get(keyProperty);
            m_classDatabase.getClassModelContext().getKeyChangeDictionary().objectRemoved(
                    getSimpleName(), oldKeyVal != null ? String.valueOf(oldKeyVal) : null, isTrackNewAndRemoved());
        }
    }

    public static void tryAndInject(Object target, Object property, String name) {
        Field field = findField(target.getClass(), name);
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(target, property);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static Field findField(Class aClass, String name) {
        try {
            Field field = aClass.getDeclaredField(name);
            return field;
        } catch (NoSuchFieldException e) {
            if (aClass.getSuperclass() != Object.class) {
                return findField(aClass.getSuperclass(), name);
            }
        }
        return null;
    }

    public Property getProperty(String name) {
        Property property = m_propertyMap.get(name.toLowerCase());
        return property != null ? property : m_propertyMapByXMLMapping.get(name);
    }

    public Vector<ObjectMeta> getAllInstancesInHierarchy(String query) {
        Vector<ObjectMeta> all = getAllInstancesInHierarchy();
        if (query == null) {
            return new Vector<ObjectMeta>();
        }
        if (query.equals("")) {
            return all;
        }
        Vector<ObjectMeta> filtered = new Vector<ObjectMeta>();
        if (!query.contains("=")) {
            for (ObjectMeta o : all) {
                if (o.getKey().startsWith(query)) {
                    filtered.add(o);
                }
            }
        }
        String[] s = query.split("=");
        for (ObjectMeta o : all) {
            Property prop = getProperty(s[0]);
            if (prop == null) continue;
            if (String.valueOf(o.get(prop)).equals(s[1])) filtered.add(o);
        }
        return filtered;
    }

    public Vector<ObjectMeta> getAllInstancesInHierarchy() {
        Vector<ObjectMeta> result = new Vector<ObjectMeta>();
        for (ClassModel<?> validImpl : validImplementations.values()) {
            if (!validImpl.equals(this)) {
                result.addAll(validImpl.getAllInstancesInHierarchy());
            }
        }
        result.addAll(allInstances());
        return result;
    }

    private Collection<ObjectMeta> allInstances() {
        Collection<ObjectMeta> result = new ArrayList<ObjectMeta>(instances.size());
        for (ObjectMeta instance : instances) {
            result.add(instance);
        }
        return result;
    }

    public Set<Object> search(Object instance, Class<?> resultType, boolean regexp, String matchPattern, boolean matchCase, Map<String, String> propMatch) {
        assert m_class.isAssignableFrom(instance.getClass());
        Set<Object> results = new HashSet<Object>();

        boolean typeMatches = resultType == null || resultType.isAssignableFrom(instance.getClass());
        if (matchPattern == null && propMatch == null && typeMatches) {
            results.add(instance);
        }

        boolean patternMatches = matchPattern == null;
        boolean propertiesMatch = propMatch == null;
        for (Property property : properties) {
            if (property.isTransient()) continue;
            if (property.isReference()) continue;
            Object propValue = property.get(instance);
            if (propValue != null) {
                Class propValueClass = propValue.getClass();
                if (!property.isImmutable()) {
                    ClassModel<?> classModel = m_classDatabase.getClassModel(propValueClass);
                    results.addAll(classModel.search(propValue, resultType, regexp, matchPattern, matchCase, propMatch));
                    continue;
                }
                if (matchPattern != null) {
                    String propValueStr = property.toString(propValue);
                    if (!matchCase) {
                        matchPattern = matchPattern.toLowerCase();
                        propValueStr = propValueStr.toLowerCase();
                    }
                    if ((regexp && propValueStr.matches(matchPattern))
                            || propValueStr.contains(matchPattern)) {
                        patternMatches = true;
                    }
                }
                if (propMatch != null && propMatch.containsKey(property.getName().toLowerCase())) {
                    String propValueStr = property.toString(propValue).toLowerCase();
                    if (propValueStr.equals(propMatch.get(property.getName().toLowerCase()))) {
                        propertiesMatch = true;
                    }
                }
                if (typeMatches && patternMatches && propertiesMatch) {
                    break;
                }
            }
        }
        if (typeMatches && patternMatches && propertiesMatch) {
            results.add(instance);
        }
        for (ListProperty listProperty : listProperties) {
            if (listProperty.isTransient()) continue;
            Collection list = listProperty.get(instance);
            if (list != null) {
                for (Object item : list) {
                    ClassModel<?> itemClassModel = m_classDatabase.getClassModel(item.getClass());
                    //System.out.println(item.getClass());
                    results.addAll(itemClassModel.search(item, resultType, regexp, matchPattern, matchCase, propMatch));
                }
            }
        }
        return results;
    }

    public <X> Vector<X> search(Object instance, Class<X> type, Set<Object> instancesSearched) {
        if (instancesSearched.contains(instance)) return new Vector<>();
        instancesSearched.add(instance);
        HashSet<X> results = new HashSet<>();
        //search for properties with target type
        for (ListProperty listProperty : listProperties) {
            Collection<?> list = listProperty.get(instance);
            if (listProperty.getContainedType().isAssignableFrom(type)) {
                results.addAll((Collection<? extends X>) list);
            } else {
                for (Object o : list) {
                    if (type.isInstance(o)) {
                        results.add(type.cast(o));
                    } else {
                        ClassModel<?> propertyClassModel = listProperty.getPropertyClassModel();
                        Vector<X> vector = propertyClassModel.search(o, type, instancesSearched);
                        results.addAll(vector);
                    }
                }
            }
        }
        for (Property property : properties) {
            Object o = property.get(instance);
            if (o == null) continue;
            if (property.getPropertyClass().isAssignableFrom(type)) {
                results.add(type.cast(o));
            } else {
                ClassModel<?> propertyClassModel = property.getPropertyClassModel();
                Vector<X> vector = propertyClassModel.search(o, type, instancesSearched);
                results.addAll(vector);
            }
        }

        return new Vector<>(results);
    }

    /**
     * finds a property whose name matches the given regular expression
     */
    public Set<Property> filterProperties(String regexp) {
        Set<Property> matches = new HashSet<>();
        for (Property property : properties) {
            if (property.getName().toLowerCase().matches(regexp.toLowerCase())) matches.add(property);
        }
        return matches;
    }

    public List<Property> getAllProperties() {
        return new ArrayList<>(m_propertyMap.values());
    }

    public List<Property> getAllProperties(Filter<Property> filter) {
        return CollectionsUtils.filter(m_propertyMap.values(), filter);
    }

    public ClassModel getValidImplementation(String className) {
        if (getSimpleName().equals(className)) return this;
        ClassModel classModel = validImplementations.get(className);
        if (classModel == null) {
            throw new XappException(className + " is not a valid implementation of " + this);
        }
        return classModel;
    }

    public Object createClone(Object instance) {
        if (!(instance instanceof Cloneable)) {
            throw new XappException("obj " + instance + " not cloneable");
        }
        try {
            Method cloneMethod = instance.getClass().getMethod("clone");
            return cloneMethod.invoke(instance);
        } catch (Exception e) {
            throw new XappException(e);
        }
    }

    public Property getKeyProperty() {
        if(keyProperty == null) {
            String keyPropName = getClassDatabase().getKeyProperty(m_class);
            if(keyPropName!=null) {
                keyProperty = getProperty(keyPropName);
                keyProperty.setKey(true);
            }
        }
        return keyProperty;
    }

    public boolean isEnum() {
        return m_class.isEnum();
    }

    public ClassDatabase getClassDatabase() {
        return m_classDatabase;
    }

    public boolean isInstance(Object obj) {
        return m_class.isInstance(obj);
    }

    public Collection<ObjectMeta> getInstances() {
        return allInstances();
    }

    public boolean isContainer() {
        return m_containerListProperty != null;
    }

    public ContainerProperty getContainerProperty() {
        return m_containerListProperty;
    }

    public static Object tryAndInvoke(Object target, Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            log.info(format("error calling method %s on %s of type %s with %s", method.getName(), target, target.getClass(), Arrays.toString(args)));
            throw new XappException(e);
        }
    }

    public static Object tryAndInvoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return tryAndInvoke(target, method);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Object tryAndInvoke(Class targetClass, String methodName) {
        try {
            Method method = targetClass.getMethod(methodName);
            return tryAndInvoke(null, method);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /*public List<ClassModel> getClassModelHeirarchy() {
        List<ClassModel> l = new ArrayList<ClassModel>(m_validImplementations);
        l.add(this);
        return l;
    }*/

    public Method getPostInitMethod() {
        return postInitMethod;
    }

    public boolean hasPostInit() {
        return postInitMethod != null;
    }

    public Method getPreInitMethod() {
        return preInitMethod;
    }

    public boolean hasPreInit() {
        return preInitMethod != null;
    }


    public TrackKeyChanges getTrackKeyChanges() {
        return m_trackKeyChanges;
    }

    public boolean isTrackNewAndRemoved() {
        return m_trackKeyChanges != null && m_trackKeyChanges.trackNewAndRemoved();
    }

    public String getXMLMapping() {
        XMLMapping xmlMapping = (XMLMapping) m_class.getAnnotation(XMLMapping.class);
        return xmlMapping != null ? xmlMapping.value() : getSimpleName();
    }

    public boolean hasMethod(String methodName, Class<?>... parameterTypes) {
        return ReflectionUtils.hasMethodInHierarchy(m_class, methodName, parameterTypes);
    }

    public void deleteAll() {
        for (ObjectMeta<T> instance : instances) {
            instance.dispose();
        }
    }

    public void registerWithClassDatabase(ObjectMeta objectMeta) {
        getClassDatabase().registerInstance(objectMeta);
    }

    public void tryAndCallPostInit(ObjectMeta objectMeta) {
        if(hasPostInit()) {
            tryAndInvoke(objectMeta.getInstance(), postInitMethod, objectMeta);
        }
    }

    public Property findOneProperty(final Class type, final Filter<Property> filter) {
        return findOneProperty(new Filter<Property>() {
            @Override
            public boolean matches(Property property) {
                return property.getMainType().isAssignableFrom(type) && filter.matches(property);
            }

            @Override
            public String toString() {
                return format("%s %s", type, filter);
            }
        });
    }
    public Property findOneProperty(final Filter<Property> filter) {
        List<Property> props = getAllProperties(filter);
        if(props.isEmpty()) {
            throw new IllegalArgumentException(format("No property found on %s:%s with filter %s", getContainedClass().getSimpleName(), this, filter));
        }
        if(props.size() > 1) {
            throw new IllegalArgumentException(format("More than 1 prop found (%s) found on %s (%s) with filter %s", props.size(), this, props, filter));
        }
        return props.get(0);
    }

    private class PrimaryKeyChangedListener implements PropertyChangeListener {

        public void propertyChanged(Property property, Object target, Object oldVal, Object newVal) {
            /**
             * if the application is initializing (i.e. unmarshalling is happening) then we do not want to register
             * primary key changes, because they are not really changes - they are caused by the unmarshaller creating
             * the datamodel from the serialized form
             */
            if (m_trackKeyChanges != null && !m_classDatabase.getClassModelContext().isInitializing()) {
                m_classDatabase.getClassModelContext().getKeyChangeDictionary().primaryKeyChange(
                        new PrimaryKeyChange(getSimpleName(), oldVal != null ? oldVal.toString() : null, newVal != null ? newVal.toString() : null, isTrackNewAndRemoved()));
            }

            //m.marshal(new PrintWriter(System.out), m_classDatabase.getClassModelContext().getKeyChangeDictionary().createChangeSet());
        }
    }

    public String getResourceAsString(String name) {
        return getResourceAsString(m_class, name);
    }

    public static String getResourceAsString(Class aClass, String name) {
        return FileUtils.readInputToString(aClass.getResourceAsStream(name));
    }

    public void restrict(Rights... rights) {
        List<Rights> r = Arrays.asList(rights);
        if (r.contains(Rights.CREATE)) {
            throw new XappException("to restrict creation restrict the list property");
        }
        m_restrictedRights.addAll(r);
        for (ClassModel validImplementation : validImplementations.values()) //and so on down the heirarchy
        {
            validImplementation.restrict(rights);
        }
    }

    public void restrictProperty(String propName, Rights... rights) {
        Property prop = getProperty(propName);
        if (prop == null) {
            throw new XappException("prop " + propName + " does not exist in " + this);
        }
        List<Rights> r = Arrays.asList(rights);
        for (ClassModel validImplementation : validImplementations.values()) {
            validImplementation.restrictProperty(propName, rights);
        }
        if (prop instanceof ListProperty) {
            ListProperty listProperty = (ListProperty) prop;
            listProperty.restrict(rights);
        } else {
            prop.setEditable(r.contains(Rights.EDIT));
            prop.setEditableOnCreation(r.contains(Rights.EDIT_ON_CREATE));
            prop.setVisibilityRestricted(r.contains(Rights.VISIBLE));
        }
    }

    public boolean isAllowed(Rights... rights) {
        for (Rights right : rights) {
            if (m_restrictedRights.contains(right)) return false;
        }
        return true;
    }

    public DiffSet diff(ClassModel<T> otherClassModel, T o1, T o2) {
        //check objects of same type
        if (this.m_class != otherClassModel.m_class)
            throw new XappException("objects of different types. " + o1 + " " + o2);
        //check objects are managed by these class models
        if (!isRegistered(o1)) throw new XappException("object " + o1 + " not found in " + this);
        if (!otherClassModel.isRegistered(o2)) {
            throw new XappException("object " + o2 + " not found in " + this);
        }
        //check objects share the same primary key - could become an optional check
        ClassDatabase otherClassDatabase = otherClassModel.getClassDatabase();
        String thisKey = keyProperty != null ? String.valueOf(keyProperty.get(o1)) : null;
        String otherKey = keyProperty != null ? String.valueOf(otherClassModel.keyProperty.get(o1)) : null;
        if (!Property.objEquals(thisKey, otherKey)) //note, it is fine if objects have no primary key
        {
            throw new XappException("primary keys not matching for objects: 1: " + thisKey + " 2: " + otherKey);
        }
        DiffSet diffSet = new DiffSet();
        //go through properties recording the diffs
        for (Property property : otherClassModel.properties) {
            if (property.isTransient()) continue;
            //get the property values
            Object thisValue = property.get(o1);
            Object otherValue = property.get(o2);
            String simpleClassName = o2.getClass().getSimpleName();
            String propertyName = property.getName();
            //if property is a simple type record the difference
            if (property.isStringPrimitiveOrEnum() || property.isReference() || property.isStringSerializable()) {   //compare only the keys if the property is a refers to another object
                if (property.isReference()) {
                    thisValue = thisValue != null ? property.getPropertyClassModel().getKey(thisValue) : null;
                    otherValue = otherValue != null ? property.getPropertyClassModel().getKey(otherValue) : null;
                }
                if (!Property.objEquals(thisValue, otherValue)) {
                    String sValue1 = property.toString(thisValue);
                    String sValue2 = property.toString(otherValue);
                    PropertyDiff diff = new PropertyDiff(simpleClassName, otherKey, propertyName, sValue1, sValue2);
                    diffSet.getPropertyDiffs().add(diff);
                }
            } else //complex type: call diff on this property's class model
            {
                if (thisValue == null && otherValue == null) //nochange
                {
                    continue;
                } else if (thisValue == null) {
                    //node added
                    Marshaller marshaller = new Marshaller(otherValue.getClass(), otherClassDatabase, true);
                    String xmlValue = marshaller.toXMLString(otherValue);
                    //need to escape the cdata in a special way
                    xmlValue = xmlValue.replace(CDATA_START, NESTED_CDATA_START);
                    xmlValue = xmlValue.replace(CDATA_END, NESTED_CDATA_END);
                    diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, xmlValue, false, null, otherValue.getClass().getName()));
                } else if (otherValue == null) {
                    //node removed
                    diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, null, true, null, null));
                } else //property changed
                {
                    ClassModel otherPropertyClassModel = otherClassDatabase.getClassModel(otherValue.getClass());
                    ClassModel thisPropertyClassModel = m_classDatabase.getClassModel(thisValue.getClass());
                    DiffSet diffsetToMerge = thisPropertyClassModel.diff(otherPropertyClassModel, thisValue, otherValue);
                    if (thisPropertyClassModel.hasKey()) {
                        diffSet.merge(diffsetToMerge);
                    } else if (!diffsetToMerge.isEmpty()) {
                        diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, null, false, diffsetToMerge, otherValue.getClass().getSimpleName()));
                    }
                }
            }
        }
        //go through list properties, iterating their contents and comparing values
        //handle list properties with @ContainReferences annotation
        //with lists of references we only need to handle added and removed nodes
        for (ListProperty listProperty : listProperties) {
            if (listProperty.isTransient()) continue;
            String propertyName = listProperty.getName();
            String containerClass = o1.getClass().getSimpleName();
            String containerKey = thisKey;
            List thisList = listProperty.castToList(o1);
            List otherList = listProperty.castToList(o2);
            List otherListOriginal = otherList;
            thisList = thisList == null ? new ArrayList() : thisList;
            otherList = otherList == null ? new ArrayList() : new ArrayList(otherList);//Copy the list coz we modify it
            Property primaryKeyProperty = listProperty.getContainedTypeClassModel().keyProperty;
            if (listProperty.containsReferences()) {
                if (thisList == null) thisList = new ArrayList();
                List<String> thisKeys = new ArrayList<String>();
                List<String> otherKeys = new ArrayList<String>();
                for (Object o : thisList) {
                    thisKeys.add(String.valueOf(primaryKeyProperty.get(o)));
                }
                for (Object o : otherList) {
                    otherKeys.add(String.valueOf(primaryKeyProperty.get(o)));
                }
                List<String> removedKeys = new ArrayList<String>(thisKeys);
                List<String> addedKeys = otherKeys;
                removedKeys.removeAll(otherKeys);
                addedKeys.removeAll(thisKeys);
                List<Integer> addedIndexList = new ArrayList<Integer>();
                for (int i = 0; i < otherList.size(); i++) {
                    Object o = otherList.get(i);
                    if (addedKeys.contains(String.valueOf(primaryKeyProperty.get(o)))) {
                        addedIndexList.add(i);
                    }
                }
                if (!removedKeys.isEmpty() || !addedKeys.isEmpty()) {
                    diffSet.getReferenceListDiffs().add(new ReferenceListDiff(containerClass, containerKey, propertyName, addedKeys, addedIndexList, removedKeys));
                }
            } else if (primaryKeyProperty != null) {
                //find removed nodes using keys
                Map thisKeys = new HashMap();
                Map otherKeys = new HashMap();
                for (Object o : thisList) {
                    thisKeys.put(primaryKeyProperty.get(o), o);
                }
                for (Object o : otherList) {
                    otherKeys.put(primaryKeyProperty.get(o), o);
                }
                //removed nodes are ones in thisKeys but not otherKeys
                thisKeys.keySet().removeAll(otherKeys.keySet());
                for (Object key : thisKeys.keySet()) {
                    String nodeClass = thisKeys.get(key).getClass().getSimpleName();
                    diffSet.getRemovedNodeDiffs().add(new RemovedNodeDiff(nodeClass, containerClass, containerKey, propertyName, (String) key));
                }

                for (Object o : thisList) {
                    ClassModel itemClassModel = m_classDatabase.getClassModel(o.getClass());
                    ClassModel otherItemClassModel = otherClassDatabase.getClassModel(o.getClass());
                    if (itemClassModel.keyProperty != null) {
                        Object key = itemClassModel.getKey(o);
                        Object otherO = otherKeys.get(key);
                        if (otherO != null) //if not removed
                        {
                            diffSet.merge(itemClassModel.diff(otherItemClassModel, o, otherO));
                            otherList.remove(otherO);
                        }
                    }
                }
                //'otherList' will now contain the added nodes
                for (Object addedObj : otherList) {
                    Marshaller marshaller = new Marshaller(addedObj.getClass(), otherClassDatabase, true);
                    String xmlValue = marshaller.toXMLString(addedObj);
                    String className = addedObj.getClass().getSimpleName();
                    //need to escape the cdata in a special way
                    xmlValue = xmlValue.replace(CDATA_START, NESTED_CDATA_START);
                    xmlValue = xmlValue.replace(CDATA_END, NESTED_CDATA_END);
                    diffSet.getNewNodeDiffs().add(new NewNodeDiff(className, containerClass, containerKey, propertyName, xmlValue, otherListOriginal.indexOf(addedObj)));
                }
            } else //the list contains 'anonymous' objects and the diff must be noted in its entirety
            {
                //marshal both lists and compare the string result
                //System.out.println("WARNING anonymous lists are not handled by diff and merge, " + listProperty );
            }
        }
        return diffSet;
    }

    private boolean isRegistered(T o1) {
        return find(o1) != null;
    }

    public ObjectMeta<T> findOrCreate(ObjectLocation objectLocation, Object o1) {
        ObjectMeta<T> objectMeta = find((T) o1);
        if(objectMeta==null) {
            objectMeta = createObjMeta(objectLocation, (T) o1, false, false);
        } else {
            //update the parent
            objectMeta.setHome(objectLocation, false);
        }
        return objectMeta;
    }

    public ObjectMeta<T> find(T o1) {
        return getClassDatabase().find(o1);
    }

    public List<ObjectMeta<T>> findAll(Collection<T> objs) {
        List<ObjectMeta<T>> result = new ArrayList<ObjectMeta<T>>();
        for (T obj : objs) {
            result.add(find(obj));
        }
        return result;
    }

    public boolean hasKey() {
        return getKeyProperty() != null;
    }

    /**
     * Takes an object of this classmodel's class and makes all the changes to it specified by the diffset
     *
     * @param obj
     * @param diffSet        //todo fix
     */
    public void merge(T obj, DiffSet diffSet) {
        if (obj.getClass() != m_class) throw new XappException("Object " + obj + " not of this class. " + this);
        ObjectMeta objectMeta = find(obj);
        //handle added nodes first in case there are other diffs refering to the new node
        for (NewNodeDiff newNodeDiff : diffSet.getNewNodeDiffs()) {
            //find object with the list
            String containerKey = newNodeDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(newNodeDiff.getContainerClass());
            ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(newNodeDiff.getNodeClass());
            Object target = containerKey == null ? obj : objectMeta.get(containerCM.getContainedClass(), containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(newNodeDiff.getListProperty());
            List list = listProperty.castToList(target);
            if (list == null) {
                list = new ArrayList();
                listProperty.set(target, list);//must set the list ref back in the merged object
            }
            Unmarshaller un = new Unmarshaller(nodeCM);
            String xml = newNodeDiff.getNewValue();
            xml = xml.replace(NESTED_CDATA_START, CDATA_START);
            xml = xml.replace(NESTED_CDATA_END, CDATA_END);
            //TODO unmarshalling a new node like this will now require passing some context to the unmarshaller
            //TODO otherwise the creation of the objmeta will fail
            Object newNode = un.unmarshalString(xml, Charset.forName("UTF-8"));
            list.add(newNodeDiff.getOriginalIndex(), newNode);
        }
        for (PropertyDiff propertyDiff : diffSet.getPropertyDiffs()) {
            //find object to change
            String key = propertyDiff.getKey();
            ClassModel classModel = m_classDatabase.getClassModelBySimpleName(propertyDiff.getClazz());
            ObjectMeta target = null;
            if (key == null) {
                target = objectMeta;
            } else {
                target = objectMeta.getObjMeta(classModel.getContainedClass(), key);
            }
            if (target != null) {
                Property property = classModel.getProperty(propertyDiff.getProperty());
                if (property == null) {
                    System.out.println("warning " + propertyDiff.getClazz() + " " + propertyDiff.getProperty() + " is null");
                }
                property.setSpecial(target, propertyDiff.getNewValue());
            }
        }
        for (ComplexPropertyDiff complexPropertyDiff : diffSet.getComplexPropertyDiffs()) {
            String key = complexPropertyDiff.getKey();
            ClassModel classModel = m_classDatabase.getClassModelBySimpleName(complexPropertyDiff.getClazz());
            ObjectMeta target = null;
            if (key == null) {
                target = objectMeta;
            } else {
                target = objectMeta.getObjMeta(classModel.getContainedClass(), key);
            }
            Property property = classModel.getProperty(complexPropertyDiff.getProperty());
            if (complexPropertyDiff.isRemoved()) {
                target.set(property, null);
            }
            //added
            else if (complexPropertyDiff.getNewValue() != null) {
                String xml = complexPropertyDiff.getNewValue();
                ClassModel newNodeCM = m_classDatabase.getClassModelByName(complexPropertyDiff.getPropertyClass());
                Unmarshaller unmarshaller = new Unmarshaller(newNodeCM);
                target.set(property, unmarshaller.unmarshalString(xml, Charset.forName("UTF-8")));
            }
            //changed
            else {
                Object o = target.get(property);
                ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(complexPropertyDiff.getPropertyClass());
                nodeCM.merge(o, complexPropertyDiff.getDiffSet());
            }
        }
        //handle lists containing refs
        for (ReferenceListDiff referenceListDiff : diffSet.getReferenceListDiffs()) {
            String containerKey = referenceListDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(referenceListDiff.getContainerClass());
            ObjectMeta target = containerKey == null ? objectMeta : objectMeta.getObjMeta(containerCM.getContainedClass(), containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(referenceListDiff.getListProperty());
            List list = listProperty.castToList(target);
            if (list == null) {
                list = new ArrayList();
                target.set(listProperty, list);
            }
            List<String> removedKeys = referenceListDiff.getRemovedNodes();
            List<String> addedKeys = referenceListDiff.getAddedNodes();
            List<Integer> addedNodeIndexes = referenceListDiff.getAddedNodeIndexes();
            ClassModel containedTypeClassModel = listProperty.getContainedTypeClassModel();
            for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                Object o = iterator.next();
                if (removedKeys.contains(containedTypeClassModel.getKey(o))) {
                    iterator.remove();
                }
            }
            for (int i = 0; i < addedKeys.size(); i++) {
                String addedKey = addedKeys.get(i);
                Object addedObj = objectMeta.get(containedTypeClassModel.getContainedClass(), addedKey);
                int index = addedNodeIndexes != null ? addedNodeIndexes.get(i) : Integer.MAX_VALUE;
                if (index <= list.size()) {
                    list.add(index, addedObj);
                } else {
                    list.add(addedObj);
                }
            }

        }

        //handle removed nodes
        for (RemovedNodeDiff removedNodeDiff : diffSet.getRemovedNodeDiffs()) {
            String containerKey = removedNodeDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(removedNodeDiff.getContainerClass());
            ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(removedNodeDiff.getNodeClass());
            ObjectMeta target = containerKey == null ? objectMeta : objectMeta.getObjMeta(containerCM.getContainedClass(), containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(removedNodeDiff.getListProperty());
            List list = listProperty.castToList(target.getInstance());
            int index = 0;
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                String oKey = (String) nodeCM.getKey(o);
                if (oKey.equals(removedNodeDiff.getKey())) {
                    index = i;
                    break;
                }
            }
            list.remove(index);
        }
    }

    public ContainerProperty getChildrenProperty() {
        if(childrenProperty == null) {
            childrenProperty = (ContainerProperty) findOneProperty(new Filter<Property>() {
                @Override
                public boolean matches(Property property) {
                    return property.getPropertyAccess().getAnnotation(Children.class) != null;
                }
            });
        }
        return childrenProperty;
    }
}
