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
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.annotations.marshalling.XMLMapping;
import net.sf.xapp.annotations.objectmodelling.PrimaryKeyScope;
import net.sf.xapp.annotations.objectmodelling.Singleton;
import net.sf.xapp.annotations.objectmodelling.TrackKeyChanges;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.Rights;
import net.sf.xapp.objectmodelling.difftracking.*;
import net.sf.xapp.tree.Tree;
import net.sf.xapp.utils.ClassUtils;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.XappException;
import net.sf.xapp.utils.FileUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Class Model wraps a {@link Class} and also encapsulates the extra djwastor meta data.
 * <p/>
 * It also stores all the instances of that class that have been created via the framework (through unmarshalling
 * or through an application gui).
 * <p/>
 * It is also responsible for the creation process of these objects. When creating managed objects the ClassModel
 * tries to "inject" through reflection an instance of itself.
 */
public class ClassModel<T>
{
    private Class<T> m_class;
    private ClassDatabase m_classDatabase;
    private List<Property> m_properties;
    private Map<String, Property> m_propertyMap;
    private Map<String, Property> m_propertyMapByXMLMapping;
    private List<ListProperty> m_listProperties;
    private List<ClassModel<? extends T>> m_validImplementations;
    private Map<String, ClassModel> m_validImplementationMap;
    private int m_nextId;
    private BoundObjectType boundObjectType;
    private EditorWidget editorWidget;
    private Property m_primaryKeyProperty;
    private List<T> m_instances;
    private HashMap<Object, T> m_mapByPrimaryKey;
    private Method m_setClassModelMethod;
    private Method m_postInitMethod;
    private ListProperty m_containerListProperty;
    private Set<Rights> m_restrictedRights;
    private TrackKeyChanges m_trackKeyChanges;

    /**
     * True if this is a singleton class.
     * This is indicated by the Singleton annotation.
     */
    private boolean singleton;

    public final String NESTED_CDATA_START = "]ATADC]!>";
    public final String NESTED_CDATA_END = ">]]";
    public final String CDATA_START = "<![CDATA[";
    public final String CDATA_END = "]]>";

    public ClassModel(ClassDatabase classDatabase,
                      Class aClass,
                      List<Property> properties,
                      List<ListProperty> listProperties,
                      List<ClassModel<? extends T>> validImplementations,
                      EditorWidget editorWidget,
                      BoundObjectType boundObjectType,
                      Property primaryKeyProperty, String containerListProp, Method postInitMethod)
    {
        m_class = aClass;

        // Set singleton status of this class
        Singleton singletonann = m_class.getAnnotation(Singleton.class);
        singleton = singletonann != null && singletonann.isSingleton();

        m_postInitMethod = postInitMethod;
        m_classDatabase = classDatabase;
        m_properties = properties;
        m_listProperties = listProperties;
        m_validImplementations = validImplementations;
        m_propertyMap = new HashMap<String, Property>();
        m_propertyMapByXMLMapping = new HashMap<String, Property>();
        for (Property property : properties)
        {
            String propNameLC = property.getName().toLowerCase();
            m_propertyMap.put(propNameLC, property);
            if (property.getXMLMapping() != null)
            {
                m_propertyMapByXMLMapping.put(property.getXMLMapping(), property);
            }
        }
        for (ListProperty property : listProperties)
        {
            m_propertyMap.put(property.getName().toLowerCase(), property);
            if (property.getXMLMapping() != null)
            {
                m_propertyMapByXMLMapping.put(property.getXMLMapping(), property);
            }
        }
        m_instances = new ArrayList<T>();
        m_mapByPrimaryKey = new HashMap<Object, T>();
        this.boundObjectType = boundObjectType;
        this.editorWidget = editorWidget;
        m_validImplementationMap = new HashMap<String, ClassModel>();
        if (m_validImplementations != null)
        {
            putAllValidImpls();
        }
        m_primaryKeyProperty = primaryKeyProperty;
        if (m_primaryKeyProperty != null)
        {
            m_primaryKeyProperty.addChangeListener(new PrimaryKeyChangedListener());
        }
        try
        {
            m_setClassModelMethod = aClass.getMethod("setClassModel", ClassModel.class);
        }
        catch (NoSuchMethodException e)
        {
        }
        if (containerListProp != null) m_containerListProperty = (ListProperty) getProperty(containerListProp);

        m_restrictedRights = new HashSet<Rights>();

        m_trackKeyChanges = (TrackKeyChanges) ClassUtils.getAnnotationInHeirarchy(TrackKeyChanges.class, m_class);
        if (m_trackKeyChanges != null && !hasPrimaryKey())
        {
            throw new XappException("class " + getSimpleName() + " is annotated with @TrackKeyChanges but has no primary key");
        }
    }

    private void putAllValidImpls()
    {
        for (ClassModel validImpl : m_validImplementations)
        {
            m_validImplementationMap.put(validImpl.getSimpleName(), validImpl);
        }
    }

    public BoundObjectType getBoundObjectType() {
        return boundObjectType;
    }

    public EditorWidget getEditorWidget() {
        return editorWidget;
    }

    public Class<T> getContainedClass()
    {
        return m_class;
    }

    public List<Property> getProperties()
    {
        return m_properties;
    }

    public List<Property> getNonTransientProperties()
    {
        List<Property> list = new ArrayList<Property>();
        for (Property property : m_properties)
        {
            if (!property.isTransient())
            {
                list.add(property);
            }
        }
        for (ListProperty listProperty : m_listProperties)
        {
            if (listProperty.hasSpecialBoundComponent())
            {
                list.add(listProperty);
            }
        }
        return list;
    }

    public List<Property> getVisibleProperties()
    {
        List<Property> props = getNonTransientProperties();
        props.addAll(getNonTransientPrimitiveLists());
        ArrayList<Property> results = new ArrayList<Property>();
        for (Property prop : props)
        {
            if (prop.isVisibilityRestricted()) {
                results.add(prop);
            }
        }
        return results;
    }

    public List<ListProperty> getNonTransientPrimitiveLists()
    {
        List<ListProperty> list = new ArrayList<ListProperty>();
        for (ListProperty property : m_listProperties)
        {
            if (!property.isTransient() && !property.hasSpecialBoundComponent() && (
                    property.getContainedType() == String.class ||
                    property.getContainedType() == Integer.class ||
                    property.getContainedType() == Long.class ||
                    property.getContainedType().isEnum()))
            {
                list.add(property);
            }
        }
        return list;
    }

    public List<ListProperty> getListProperties()
    {
        return m_listProperties;
    }

    public String toString()
    {
        return getSimpleName();
    }

    public String getSimpleName()
    {
        return m_class.getSimpleName();
    }

    public List<ClassModel<? extends T>> getValidImplementations()
    {
        return m_validImplementations;
    }

    public String getName(Object target)
    {
        Property property = m_propertyMap.get("name");
        if (property != null)
        {
            Object o = property.get(target);
            return o != null ? String.valueOf(o) : target.toString();
        }
        else
        {
            return target.toString();
        }
    }

    public boolean isAbstract()
    {
        return !m_validImplementations.isEmpty();
    }

    public int getNoOfProperties()
    {
        return m_listProperties.size() + m_properties.size();
    }

    /**
     * Create a new instance of the class.
     * If the class is marked as a singleton by the @Singleton annotation
     * a new instance will be created only if none already exists.
     * <p/>
     * Note that instances created by ordinary means will not be limited
     * by these restrictions as they are not created using this mechanism
     * and thus not become registered by the framework.
     *
     * @return the new instance or existing singleton instance.
     */
   /* public synchronized T newInstance()
    {
        return newInstance(null);
    }*/

    /**
     * Create a new instance of the class.
     * If the class is marked as a singleton by the @Singleton annotation
     * a new instance will be created only if none already exists.
     * <p/>
     * Note that instances created by ordinary means will not be limited
     * by these restrictions as they are not created using this mechanism
     * and thus not become registered by the framework.
     *
     * @return the new instance or existing singleton instance.
     */
    public synchronized T newInstance()
    {
        return newInstance(null);
    }
    public synchronized T newInstance(Object parent)
    {
        if (singleton && m_instances.size() == 1)
        {
            return m_instances.iterator().next();
        }
        else
        {
            try
            {
                T obj = m_class.newInstance();
                registerInstance(obj, parent);
                injectClassModel(obj);
                return obj;
            }
            catch (Exception e)
            {
                System.out.println("cannot create instance of " + m_class);
                throw new XappException(e);
            }
        }
    }

    private int registerInstance(T obj, Object parent)
    {
        int nextId = m_nextId++;
        m_instances.add(obj);
        if (parent!=null)
        {
            //m_parentMap.put(obj, parent);
        }
        return nextId;
    }

    /**
     * Maps the given object by its primary key. May be necessary to call this from outside if
     * the object was created externally. This cannot currently have been called by {@link #newInstance()}
     * because the primary key would not have been set.
     *
     * @param obj
     */
    public void mapByPrimaryKey(T obj)
    {
        if (!obj.getClass().equals(m_class))
            throw new IllegalArgumentException(obj + " is not of class " + m_class.getSimpleName());

        //assert m_primaryKeyProperty != null : m_class.getSimpleName() + " has no primary key";

        //register the instance if unknown to us
        if (!m_instances.contains(obj))
        {
            registerInstance(obj, null);
        }

        Object key = getPrimaryKey(obj);
        Object o = m_mapByPrimaryKey.put(key, obj);
        //if (o != null)
        //    System.out.println("duplicate key for " + obj + " and " + o+ " key: "+key);
    }

    public String getPrimaryKey(T obj)
    {
        PrimaryKeyScope uniqueScope = m_primaryKeyProperty.getUniqueScope();
        String myKey = String.valueOf(m_primaryKeyProperty.get(obj));
        switch (uniqueScope)
        {
            case GLOBAL:
                return myKey;
            case PARENT:
                /*Object parentObj = m_parentMap.get(obj);
                String path = "";
                if (parentObj!=null)
                {
                    path = getClassDatabase().getClassModel(parentObj.getClass()).getPrimaryKey(parentObj) + ".";
                }
                return path + myKey;*/
                throw new UnsupportedOperationException("Operation disabled because unreliable, needs re-work");
        }
        return null;
    }

    public void dispose(Object instance)
    {
        m_instances.remove(instance);
        //m_parentMap.remove(instance);
        if (m_primaryKeyProperty != null)
        {
            Object key = m_primaryKeyProperty.get(instance);
            m_mapByPrimaryKey.remove(key);
            m_classDatabase.getClassModelContext().getKeyChangeDictionary().objectRemoved(getSimpleName(), key != null ? String.valueOf(key) : null, isTrackNewAndRemoved());
        }
    }

    /**
     * Removes an instance of the class and recursively deletes referenced objects.
     * Properties marked with {@link @Reference} or {@link @ContainReferences} are skipped
     *
     * @param instance
     */
    public void delete(Object instance)
    {
        for (Property property : m_properties)
        {
            if (property.isTransient()) continue;
            if (property.isImmutable()) continue;
            if (property.isReference()) continue;
            Object value = property.get(instance);
            if (value != null)
            {
                m_classDatabase.getClassModel(value.getClass()).delete(value);
            }
        }
        for (ListProperty listProperty : m_listProperties)
        {
            if (listProperty.containsReferences()) continue;
            if (listProperty.isTransient()) continue;
            Collection list = listProperty.get(instance);
            if (list != null)
            {
                for (Object item : list)
                {
                    m_classDatabase.getClassModel(item.getClass()).delete(item);
                }
            }
        }
        dispose(instance);
    }

    public static void tryAndInject(Object target, Object property, String name)
    {
        Field field = findField(target.getClass(), name);
        if (field != null)
        {
            field.setAccessible(true);
            try
            {
                field.set(target, property);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    private static Field findField(Class aClass, String name)
    {
        try
        {
            Field field = aClass.getDeclaredField(name);
            return field;
        }
        catch (NoSuchFieldException e)
        {
            if (aClass.getSuperclass() != Object.class)
            {
                return findField(aClass.getSuperclass(), name);
            }
        }
        return null;
    }

    public Property getProperty(String name)
    {
        Property property = m_propertyMap.get(name.toLowerCase());
        return property != null ? property : m_propertyMapByXMLMapping.get(name);
    }

    public Vector<T> getAllInstancesInHierarchy(String query)
    {
        Vector<T> all = getAllInstancesInHierarchy();
        if(query == null)
        {
            return new Vector<T>();
        }
        if (query.equals(""))
        {
            return all;
        }
        Vector<T> filtered = new Vector<T>();
        if(!query.contains("="))
        {
            for (T o : all)
            {
                if(getPrimaryKey(o).startsWith(query))
                {
                    filtered.add(o);
                }
            }
        }
        String[] s = query.split("=");
        for (T o : all)
        {
            Property prop = getProperty(s[0]);
            if (prop == null) continue;
            if (String.valueOf(prop.get(o)).equals(s[1])) filtered.add(o);
        }
        return filtered;
    }

    public Vector<T> getAllInstancesInHierarchy()
    {
        Vector<T> instances = new Vector<T>();
        for (ClassModel validImpl : m_validImplementations)
        {
            if (validImpl.equals(this)) continue;
            instances.addAll(validImpl.getAllInstancesInHierarchy());
        }
        instances.addAll(m_instances);
        return instances;
    }

    public Set<Object> search(Object instance, Class resultType, boolean regexp, String matchPattern, boolean matchCase, Map<String, String> propMatch)
    {
        assert m_class.isAssignableFrom(instance.getClass());
        Set<Object> results = new HashSet<Object>();

        boolean typeMatches = resultType == null || resultType.isAssignableFrom(instance.getClass());
        if (matchPattern == null && propMatch == null && typeMatches)
        {
            results.add(instance);
        }

        boolean patternMatches = matchPattern == null;
        boolean propertiesMatch = propMatch == null;
        for (Property property : m_properties)
        {
            if (property.isTransient()) continue;
            if (property.isReference()) continue;
            Object propValue = property.get(instance);
            if (propValue != null)
            {
                Class propValueClass = propValue.getClass();
                if (!property.isImmutable())
                {
                    results.addAll(m_classDatabase.getClassModel(propValueClass).search(propValue, resultType, regexp, matchPattern, matchCase, propMatch));
                    continue;
                }
                if (matchPattern != null)
                {
                    String propValueStr = property.toString(propValue);
                    if (!matchCase)
                    {
                        matchPattern = matchPattern.toLowerCase();
                        propValueStr = propValueStr.toLowerCase();
                    }
                    if ((regexp && propValueStr.matches(matchPattern))
                            || propValueStr.contains(matchPattern))
                    {
                        patternMatches = true;
                    }
                }
                if (propMatch != null && propMatch.containsKey(property.getName().toLowerCase()))
                {
                    String propValueStr = property.toString(propValue).toLowerCase();
                    if (propValueStr.equals(propMatch.get(property.getName().toLowerCase())))
                    {
                        propertiesMatch = true;
                    }
                }
                if (typeMatches && patternMatches && propertiesMatch)
                {
                    break;
                }
            }
        }
        if (typeMatches && patternMatches && propertiesMatch)
        {
            results.add(instance);
        }
        for (ListProperty listProperty : m_listProperties)
        {
            if (listProperty.isTransient()) continue;
            Collection list = listProperty.get(instance);
            if (list != null)
            {
                for (Object item : list)
                {
                    ClassModel itemClassModel = m_classDatabase.getClassModel(item.getClass());
                    //System.out.println(item.getClass());
                    results.addAll(itemClassModel.search(item, resultType, regexp, matchPattern, matchCase, propMatch));
                }
            }
        }
        return results;
    }

    public Vector search(Object instance, Class type, Set instancesSearched)
    {
        if (instancesSearched.contains(instance)) return new Vector();
        instancesSearched.add(instance);
        HashSet results = new HashSet();
        //search for properties with target type
        for (ListProperty listProperty : m_listProperties)
        {
            Collection list = listProperty.get(instance);
            if (listProperty.getContainedType().isAssignableFrom(type))
            {
                results.addAll(list);
            }
            else
            {
                for (Object o : list)
                {
                    if (type.isInstance(o))
                    {
                        results.add(o);
                    }
                    else
                    {
                        ClassModel propertyClassModel = listProperty.getPropertyClassModel();
                        Vector vector = propertyClassModel.search(o, type, instancesSearched);
                        results.addAll(vector);
                    }
                }
            }
        }
        for (Property property : m_properties)
        {
            Object o = property.get(instance);
            if (o == null) continue;
            if (property.getPropertyClass().isAssignableFrom(type))
            {
                results.add(o);
            }
            else
            {
                Vector vector = property.getPropertyClassModel().search(o, type, instancesSearched);
                results.addAll(vector);
            }
        }

        return new Vector(results);
    }

    /**
     * finds a property whose name matches the given regular expression
     *
     * @param regexp
     * @return
     */
    public Set<Property> filterProperties(String regexp)
    {
        Set<Property> matches = new HashSet<Property>();
        for (Property property : m_properties)
        {
            if (property.getName().toLowerCase().matches(regexp.toLowerCase())) matches.add(property);
        }
        return matches;
    }

    public List<Property> getAllProperties()
    {
        return new ArrayList<Property>(m_propertyMap.values());
    }

    public ClassModel getValidImplementation(String className)
    {
        if (getSimpleName().equals(className)) return this;
        ClassModel classModel = m_validImplementationMap.get(className);
        if (classModel == null) throw new XappException(className + " is not a valid implementation of " + this);
        return classModel;
    }

    public T getInstance(Object primaryKey)
    {
        T obj = getInstanceInternal(primaryKey);
        if (obj == null)
        {
            throw new XappException(this + " with key " + primaryKey + " does not exist");
        }
        return obj;
    }

    public T getInstanceNoCheck(Object primaryKey)
    {
        return getInstanceInternal(primaryKey);
    }

    private T getInstanceInternal(Object primaryKey)
    {
        if (m_primaryKeyProperty == null)
            throw new XappException("class " + m_class.getSimpleName() + " not annotated with @PrimaryKey");

        T instance = m_mapByPrimaryKey.get(primaryKey);
        if (instance != null) return instance;

        //try searching sub types
        for (ClassModel<? extends T> validImpl : m_validImplementations)
        {
            instance = validImpl.getInstanceInternal(primaryKey);
            if (instance != null) return instance;
        }
        return null;
    }

    public Object createClone(Object instance)
    {
        if (!(instance instanceof Cloneable)) throw new XappException("obj " + instance + " not cloneable");
        try
        {
            Method cloneMethod = instance.getClass().getMethod("clone");
            return cloneMethod.invoke(instance);
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    public Property getPrimaryKeyProperty()
    {
        return m_primaryKeyProperty;
    }

    public boolean isEnum()
    {
        return m_class.isEnum();
    }

    public ClassDatabase getClassDatabase()
    {
        return m_classDatabase;
    }

    public boolean isInstance(Object obj)
    {
        return m_class.isInstance(obj);
    }

    public void injectClassModel(Object obj)
    {
        assert isInstance(obj);
        if (m_setClassModelMethod == null) return;
        try
        {
            m_setClassModelMethod.invoke(obj, this);
        }
        catch (IllegalAccessException e)
        {
            throw new XappException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new XappException(e);
        }
    }

    public Collection getInstances()
    {
        return m_instances;
    }


    /**
     * Return an existing singleton instance for the class.
     * If no instance exists, it is created.
     * <p/>
     * An exception is thrown if two or more instances exist.  This will
     * happen if instances have been created with newInstance. A possible way
     * to prevent this possibility is to mark the class as being a singleton
     * and change newInstance to enforce this.
     *
     * @return the singleton instance.
     */
    public Object getSingleton()
    {
        int ninstances = m_instances.size();

        switch (ninstances)
        {
        case 0:
            return newInstance(null);

        case 1:
            return m_instances.iterator().next();

        default:
            throw new XappException(this + " is not a singleton. There are " + m_instances.size() + " instances");
        }
    }


    public boolean isContainer()
    {
        return m_containerListProperty != null;
    }

    public ListProperty getContainerListProperty()
    {
        return m_containerListProperty;
    }

    public static Object tryAndInvoke(Object target, Method method)
    {
        try
        {
            return method.invoke(target);
        }
        catch (IllegalAccessException e)
        {
            throw new XappException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new XappException(e);
        }
    }

    public static Object tryAndInvoke(Object target, String methodName)
    {
        try
        {
            Method method = target.getClass().getMethod(methodName);
            return tryAndInvoke(target, method);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    public static Object tryAndInvoke(Class targetClass, String methodName)
    {
        try
        {
            Method method = targetClass.getMethod(methodName);
            return tryAndInvoke(null, method);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    public List<ClassModel> getClassModelHeirarchy()
    {
        List<ClassModel> l = new ArrayList<ClassModel>(m_validImplementations);
        l.add(this);
        return l;
    }

    public void addValidImplementations(List<ClassModel<? extends T>> extraTypes)
    {
        m_validImplementations.addAll(extraTypes);
        putAllValidImpls();
    }

    public Method getPostInitMethod()
    {
        return m_postInitMethod;
    }

    public boolean hasPostInitMethod()
    {
        return m_postInitMethod != null;
    }

    public boolean isTreeType()
    {
        return Tree.class.isAssignableFrom(getContainedClass());
    }

    /**
     * @return the root for this tree type, will blow up if {@link #isTreeType()} returns false
     */
    public Tree getTreeRoot()
    {
        return ((Tree) getAllInstancesInHierarchy().get(0)).root();
    }

    public TrackKeyChanges getTrackKeyChanges()
    {
        return m_trackKeyChanges;
    }

    public boolean isTrackNewAndRemoved()
    {
        return m_trackKeyChanges != null && m_trackKeyChanges.trackNewAndRemoved();
    }

    public String getXMLMapping()
    {
        XMLMapping xmlMapping = (XMLMapping) m_class.getAnnotation(XMLMapping.class);
        return xmlMapping != null ? xmlMapping.value() : getSimpleName();
    }

    public void mapAllByPrimaryKey()
    {
        if (hasPrimaryKey())
        {
            for (T instance : m_instances)
            {
                mapByPrimaryKey(instance);
            }
        }
    }

    public boolean hasMethod(String methodName, Class<?>... parameterTypes)
    {
        return ReflectionUtils.hasMethodInHierarchy(m_class, methodName, parameterTypes);
    }

    private class PrimaryKeyChangedListener implements PropertyChangeListener
    {
        Marshaller m = new Marshaller(ChangeSet.class);

        public void propertyChanged(Property property, Object target, Object oldVal, Object newVal)
        {
            m_mapByPrimaryKey.remove(oldVal);
            if (m_mapByPrimaryKey.containsKey(newVal))
            {
                //throw new DJWastorException("obj already exists with key: "+newVal);
            }
            m_mapByPrimaryKey.put(newVal, (T) target);

            /**
             * if the application is initializing (i.e. unmarshalling is happening) then we do not want to register
             * primary key changes, because they are not really changes - they are caused by the unmarshaller creating
             * the datamodel from the serialized form
             */
            if (m_trackKeyChanges != null && !m_classDatabase.getClassModelContext().isInitializing())
            {
                m_classDatabase.getClassModelContext().getKeyChangeDictionary().primaryKeyChange(
                        new PrimaryKeyChange(getSimpleName(), oldVal != null ? oldVal.toString() : null, newVal != null ? newVal.toString() : null, isTrackNewAndRemoved()));
            }

            //m.marshal(new PrintWriter(System.out), m_classDatabase.getClassModelContext().getKeyChangeDictionary().createChangeSet());
        }
    }

    public String getResourceAsString(String name)
    {
        return getResourceAsString(m_class, name);
    }

    public static String getResourceAsString(Class aClass, String name)
    {
        return FileUtils.readInputToString(aClass.getResourceAsStream(name));
    }

    public void restrict(Rights... rights)
    {
        List<Rights> r = Arrays.asList(rights);
        if (r.contains(Rights.CREATE))
        {
            throw new XappException("to restrict creation restrict the list property");
        }
        m_restrictedRights.addAll(r);
        for (ClassModel validImplementation : m_validImplementations) //and so on down the heirarchy
        {
            validImplementation.restrict(rights);
        }
    }

    public void restrictProperty(String propName, Rights... rights)
    {
        Property prop = getProperty(propName);
        if (prop == null)
        {
            throw new XappException("prop " + propName + " does not exist in " + this);
        }
        List<Rights> r = Arrays.asList(rights);
        for (ClassModel validImplementation : m_validImplementations)
        {
            validImplementation.restrictProperty(propName, rights);
        }
        if (prop instanceof ListProperty)
        {
            ListProperty listProperty = (ListProperty) prop;
            listProperty.restrict(rights);
        }
        else
        {
            prop.setEditable(r.contains(Rights.EDIT));
            prop.setEditableOnCreation(r.contains(Rights.EDIT_ON_CREATE));
            prop.setVisibilityRestricted(r.contains(Rights.VISIBLE));
        }
    }

    public boolean isAllowed(Rights... rights)
    {
        for (Rights right : rights)
        {
            if (m_restrictedRights.contains(right)) return false;
        }
        return true;
    }

    public DiffSet diff(ClassModel<T> otherClassModel, Object o1, Object o2)
    {
        //check objects of same type
        if (this.m_class != otherClassModel.m_class)
            throw new XappException("objects of different types. " + o1 + " " + o2);
        //check objects are managed by these class models
        if (!m_instances.contains(o1)) throw new XappException("object " + o1 + " not found in " + this);
        if (!otherClassModel.m_instances.contains(o2))
        {
            throw new XappException("object " + o2 + " not found in " + this);
        }
        //check objects share the same primary key - could become an optional check
        ClassDatabase otherClassDatabase = otherClassModel.getClassDatabase();
        String thisKey = m_primaryKeyProperty != null ? String.valueOf(m_primaryKeyProperty.get(o1)) : null;
        String otherKey = m_primaryKeyProperty != null ? String.valueOf(otherClassModel.m_primaryKeyProperty.get(o1)) : null;
        if (!Property.objEquals(thisKey, otherKey)) //note, it is fine if objects have no primary key
        {
            throw new XappException("primary keys not matching for objects: 1: " + thisKey + " 2: " + otherKey);
        }
        DiffSet diffSet = new DiffSet();
        //go through properties recording the diffs
        for (Property property : otherClassModel.m_properties)
        {
            if (property.isTransient()) continue;
            //get the property values
            Object thisValue = property.get(o1);
            Object otherValue = property.get(o2);
            String simpleClassName = o2.getClass().getSimpleName();
            String propertyName = property.getName();
            //if property is a simple type record the difference
            if (property.isStringPrimitiveOrEnum() || property.isReference() || property.isStringSerializable())
            {   //compare only the keys if the property is a refers to another object
                if (property.isReference())
                {
                    thisValue = thisValue != null ? property.getPropertyClassModel().getPrimaryKey(thisValue) : null;
                    otherValue = otherValue != null ? property.getPropertyClassModel().getPrimaryKey(otherValue) : null;
                }
                if (!Property.objEquals(thisValue, otherValue))
                {
                    String sValue1 = property.toString(thisValue);
                    String sValue2 = property.toString(otherValue);
                    PropertyDiff diff = new PropertyDiff(simpleClassName, otherKey, propertyName, sValue1, sValue2);
                    diffSet.getPropertyDiffs().add(diff);
                }
            }
            else //complex type: call diff on this property's class model
            {
                if (thisValue == null && otherValue == null) //nochange
                {
                    continue;
                }
                else if (thisValue == null)
                {
                    //node added
                    Marshaller marshaller = new Marshaller(otherValue.getClass(), otherClassDatabase, true);
                    String xmlValue = marshaller.toXMLString(otherValue);
                    //need to escape the cdata in a special way
                    xmlValue = xmlValue.replace(CDATA_START, NESTED_CDATA_START);
                    xmlValue = xmlValue.replace(CDATA_END, NESTED_CDATA_END);
                    diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, xmlValue, false, null, otherValue.getClass().getName()));
                }
                else if (otherValue == null)
                {
                    //node removed
                    diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, null, true, null, null));
                }
                else //property changed
                {
                    ClassModel otherPropertyClassModel = otherClassDatabase.getClassModel(otherValue.getClass());
                    ClassModel thisPropertyClassModel = m_classDatabase.getClassModel(thisValue.getClass());
                    DiffSet diffsetToMerge = thisPropertyClassModel.diff(otherPropertyClassModel, thisValue, otherValue);
                    if (thisPropertyClassModel.hasPrimaryKey())
                    {
                        diffSet.merge(diffsetToMerge);
                    }
                    else if (!diffsetToMerge.isEmpty())
                    {
                        diffSet.getComplexPropertyDiffs().add(new ComplexPropertyDiff(simpleClassName, otherKey, propertyName, null, null, false, diffsetToMerge, otherValue.getClass().getSimpleName()));
                    }
                }
            }
        }
        //go through list properties, iterating their contents and comparing values
        //handle list properties with @ContainReferences annotation
        //with lists of references we only need to handle added and removed nodes
        for (ListProperty listProperty : m_listProperties)
        {
            if (listProperty.isTransient()) continue;
            String propertyName = listProperty.getName();
            String containerClass = o1.getClass().getSimpleName();
            String containerKey = thisKey;
            List thisList = listProperty.castToList(o1);
            List otherList = listProperty.castToList(o2);
            List otherListOriginal = otherList;
            thisList = thisList == null ? new ArrayList() : thisList;
            otherList = otherList == null ? new ArrayList() : new ArrayList(otherList);//Copy the list coz we modify it
            Property primaryKeyProperty = listProperty.getContainedTypeClassModel().m_primaryKeyProperty;
            if (listProperty.containsReferences())
            {
                if (thisList == null) thisList = new ArrayList();
                List<String> thisKeys = new ArrayList<String>();
                List<String> otherKeys = new ArrayList<String>();
                for (Object o : thisList)
                {
                    thisKeys.add(String.valueOf(primaryKeyProperty.get(o)));
                }
                for (Object o : otherList)
                {
                    otherKeys.add(String.valueOf(primaryKeyProperty.get(o)));
                }
                List<String> removedKeys = new ArrayList<String>(thisKeys);
                List<String> addedKeys = otherKeys;
                removedKeys.removeAll(otherKeys);
                addedKeys.removeAll(thisKeys);
                List<Integer> addedIndexList = new ArrayList<Integer>();
                for (int i = 0; i < otherList.size(); i++)
                {
                    Object o = otherList.get(i);
                    if (addedKeys.contains(String.valueOf(primaryKeyProperty.get(o))))
                    {
                        addedIndexList.add(i);
                    }
                }
                if (!removedKeys.isEmpty() || !addedKeys.isEmpty())
                {
                    diffSet.getReferenceListDiffs().add(new ReferenceListDiff(containerClass, containerKey, propertyName, addedKeys, addedIndexList, removedKeys));
                }
            }
            else if (primaryKeyProperty != null)
            {
                //find removed nodes using keys
                Map thisKeys = new HashMap();
                Map otherKeys = new HashMap();
                for (Object o : thisList)
                {
                    thisKeys.put(primaryKeyProperty.get(o), o);
                }
                for (Object o : otherList)
                {
                    otherKeys.put(primaryKeyProperty.get(o), o);
                }
                //removed nodes are ones in thisKeys but not otherKeys
                thisKeys.keySet().removeAll(otherKeys.keySet());
                for (Object key : thisKeys.keySet())
                {
                    String nodeClass = thisKeys.get(key).getClass().getSimpleName();
                    diffSet.getRemovedNodeDiffs().add(new RemovedNodeDiff(nodeClass, containerClass, containerKey, propertyName, (String) key));
                }

                for (Object o : thisList)
                {
                    ClassModel itemClassModel = m_classDatabase.getClassModel(o.getClass());
                    ClassModel otherItemClassModel = otherClassDatabase.getClassModel(o.getClass());
                    if (itemClassModel.m_primaryKeyProperty != null)
                    {
                        Object key = itemClassModel.getPrimaryKey(o);
                        Object otherO = otherKeys.get(key);
                        if (otherO != null) //if not removed
                        {
                            diffSet.merge(itemClassModel.diff(otherItemClassModel, o, otherO));
                            otherList.remove(otherO);
                        }
                    }
                }
                //'otherList' will now contain the added nodes
                for (Object addedObj : otherList)
                {
                    Marshaller marshaller = new Marshaller(addedObj.getClass(), otherClassDatabase, true);
                    String xmlValue = marshaller.toXMLString(addedObj);
                    String className = addedObj.getClass().getSimpleName();
                    //need to escape the cdata in a special way
                    xmlValue = xmlValue.replace(CDATA_START, NESTED_CDATA_START);
                    xmlValue = xmlValue.replace(CDATA_END, NESTED_CDATA_END);
                    diffSet.getNewNodeDiffs().add(new NewNodeDiff(className, containerClass, containerKey, propertyName, xmlValue, otherListOriginal.indexOf(addedObj)));
                }
            }
            else //the list contains 'anonymous' objects and the diff must be noted in its entirety
            {
                //marshal both lists and compare the string result
                //System.out.println("WARNING anonymous lists are not handled by diff and merge, " + listProperty );
            }
        }
        return diffSet;
    }

    public boolean hasPrimaryKey()
    {
        return getPrimaryKeyProperty() != null;
    }

    /**
     * Takes an object of this classmodel's class and makes all the changes to it specified by the diffset
     *
     * @param obj
     * @param diffSet
     */
    public void merge(Object obj, DiffSet diffSet)
    {
        if (obj.getClass() != m_class) throw new XappException("Object " + obj + " not of this class. " + this);
        //handle added nodes first in case there are other diffs refering to the new node
        for (NewNodeDiff newNodeDiff : diffSet.getNewNodeDiffs())
        {
            //find object with the list
            String containerKey = newNodeDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(newNodeDiff.getContainerClass());
            ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(newNodeDiff.getNodeClass());
            Object target = containerKey == null ? obj : containerCM.getInstance(containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(newNodeDiff.getListProperty());
            List list = listProperty.castToList(target);
            if (list == null)
            {
                list = new ArrayList();
                listProperty.set(target, list);//must set the list ref back in the merged object
            }
            Unmarshaller un = new Unmarshaller(nodeCM);
            String xml = newNodeDiff.getNewValue();
            xml = xml.replace(NESTED_CDATA_START, CDATA_START);
            xml = xml.replace(NESTED_CDATA_END, CDATA_END);
            Object newNode = un.unmarshalString(xml, Charset.forName("UTF-8"));
            list.add(newNodeDiff.getOriginalIndex(), newNode);
        }
        for (PropertyDiff propertyDiff : diffSet.getPropertyDiffs())
        {
            //find object to change
            String key = propertyDiff.getKey();
            ClassModel classModel = m_classDatabase.getClassModelBySimpleName(propertyDiff.getClazz());
            Object target = null;
            if (key == null)
            {
                target = obj;
            }
            else
            {
                target = m_classDatabase.getClassModelContext().resolveInstance(classModel, key);
            }
            if (target != null)
            {
                Property property = classModel.getProperty(propertyDiff.getProperty());
                if (property == null)
                {
                    System.out.println("warning " + propertyDiff.getClazz() + " " + propertyDiff.getProperty() + " is null");
                }
                property.setSpecial(target, propertyDiff.getNewValue());
            }
        }
        for (ComplexPropertyDiff complexPropertyDiff : diffSet.getComplexPropertyDiffs())
        {
            String key = complexPropertyDiff.getKey();
            ClassModel classModel = m_classDatabase.getClassModelBySimpleName(complexPropertyDiff.getClazz());
            Object target = null;
            if (key == null)
            {
                target = obj;
            }
            else
            {
                target = m_classDatabase.getClassModelContext().resolveInstance(classModel, key);
            }
            Property property = classModel.getProperty(complexPropertyDiff.getProperty());
            if (complexPropertyDiff.isRemoved())
            {
                property.set(target, null);
            }
            //added
            else if (complexPropertyDiff.getNewValue() != null)
            {
                String xml = complexPropertyDiff.getNewValue();
                ClassModel newNodeCM = m_classDatabase.getClassModelByName(complexPropertyDiff.getPropertyClass());
                Unmarshaller unmarshaller = new Unmarshaller(newNodeCM);
                property.set(target, unmarshaller.unmarshalString(xml, Charset.forName("UTF-8")));
            }
            //changed
            else
            {
                Object o = property.get(target);
                ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(complexPropertyDiff.getPropertyClass());
                nodeCM.merge(o, complexPropertyDiff.getDiffSet());
            }
        }
        //handle lists containing refs
        for (ReferenceListDiff referenceListDiff : diffSet.getReferenceListDiffs())
        {
            String containerKey = referenceListDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(referenceListDiff.getContainerClass());
            Object target = containerKey == null ? obj : containerCM.getInstance(containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(referenceListDiff.getListProperty());
            List list = listProperty.castToList(target);
            if (list == null)
            {
                list = new ArrayList();
                listProperty.set(target, list);//must set the list ref back in the merged object
            }
            List<String> removedKeys = referenceListDiff.getRemovedNodes();
            List<String> addedKeys = referenceListDiff.getAddedNodes();
            List<Integer> addedNodeIndexes = referenceListDiff.getAddedNodeIndexes();
            ClassModel containedTypeClassModel = listProperty.getContainedTypeClassModel();
            for (Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                Object o = iterator.next();
                if (removedKeys.contains(containedTypeClassModel.getPrimaryKey(o)))
                {
                    iterator.remove();
                }
            }
            for (int i = 0; i < addedKeys.size(); i++)
            {
                String addedKey = addedKeys.get(i);
                Object addedObj = containedTypeClassModel.getInstance(addedKey);
                int index = addedNodeIndexes != null ? addedNodeIndexes.get(i) : Integer.MAX_VALUE;
                if (index <= list.size())
                {
                    list.add(index, addedObj);
                }
                else
                {
                    list.add(addedObj);
                }
            }

        }

        //handle removed nodes
        for (RemovedNodeDiff removedNodeDiff : diffSet.getRemovedNodeDiffs())
        {
            String containerKey = removedNodeDiff.getContainerKey();
            ClassModel containerCM = m_classDatabase.getClassModelBySimpleName(removedNodeDiff.getContainerClass());
            ClassModel nodeCM = m_classDatabase.getClassModelBySimpleName(removedNodeDiff.getNodeClass());
            Object target = containerKey == null ? obj : containerCM.getInstance(containerKey);
            ListProperty listProperty = (ListProperty) containerCM.getProperty(removedNodeDiff.getListProperty());
            List list = listProperty.castToList(target);
            int index = 0;
            for (int i = 0; i < list.size(); i++)
            {
                Object o = list.get(i);
                String oKey = (String) nodeCM.getPrimaryKey(o);
                if (oKey.equals(removedNodeDiff.getKey()))
                {
                    index = i;
                    break;
                }
            }
            list.remove(index);
        }
    }
}
