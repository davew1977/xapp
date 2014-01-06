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

import net.sf.xapp.annotations.application.SimpleType;
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.marshalling.stringserializers.*;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.ClassModelContext;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.api.MarshallingContext;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeDictionary;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeHistory;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeHistoryImpl;
import net.sf.xapp.utils.XappException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * ClassModelManager is the central database for Classes, their ClassModels and their instances.
 * Usually an application has one instance of ClassModelManager, but this is not necessarily so. For example
 * if you have an application that launches mini applications of the same type. Each "mini application" gets their
 * own instance of ClassModelManager, effectively to give them their own object namespace. I.e. objects in different
 * mini applications may have the same primary key.
 */
public class ClassModelManager<T> implements ClassDatabase<T>, MarshallingContext<T>, ClassModelContext
{
    private Class m_rootType;
    private AtomicLong m_idSequence;
    private Map<Long, Object> m_instanceMap;
    private Map<Object, Long> m_idMap;
    private HashMap<Class, ClassModel> m_classModelMap;
    private HashMap<String, ClassModel> m_classModelBySimpleClassNameMap;//e.g. "Config" -> Config Class Model
    private HashMap<Class, StringSerializer> m_ssMap;
    private HashMap<Object, String> m_resourceMap;
    private HashMap<PropertyObjectPair, String> m_resourceMapByReference;
    private HashSet<Class> m_simpleTypes;
    private KeyChangeHistory m_keyChangeHistory;

    /**
     * this flag is false until the root unmarshaller has finished unmarshalling the root document
     */
    private boolean m_initializing;
    private T m_rootInstance;
    private InspectionType m_inspectionType;

    public ClassModelManager(Class<T> rootType)
    {
        this(rootType, InspectionType.METHOD);
    }
    public <T> ClassModelManager(Class<T> rootType, InspectionType inspectionType)
    {
        m_inspectionType = inspectionType;
        m_rootType = rootType;
        m_classModelMap = new HashMap<Class, ClassModel>();
        m_classModelBySimpleClassNameMap = new HashMap<String, ClassModel>();
        m_ssMap = new HashMap<Class, StringSerializer>();
        m_resourceMap = new HashMap<Object, String>();
        m_resourceMapByReference = new HashMap<PropertyObjectPair, String>();
        m_simpleTypes = new HashSet<Class>();
        m_simpleTypes.add(Integer.class);
        m_simpleTypes.add(Long.class);
        m_simpleTypes.add(Boolean.class);
        m_simpleTypes.add(Double.class);
        m_simpleTypes.add(Float.class);
        m_simpleTypes.add(Character.class);
        m_simpleTypes.add(Byte.class);
        m_simpleTypes.add(Short.class);
        m_simpleTypes.add(String.class);

        m_ssMap.put(String[].class, new StringArraySerializer());
        m_ssMap.put(int[].class, new IntegerArraySerializer());
        m_ssMap.put(long[].class, new LongListSerializer());
        m_ssMap.put(Date.class, new DateStringSerializer());

        m_keyChangeHistory = new KeyChangeHistoryImpl();

        m_initializing = true;
    }

    public void setMarshalDatesAsLongs() {
        m_ssMap.put(Date.class, new DateLongSerializer());
    }

    public Unmarshaller getRootUnmarshaller()
    {
        return createUnmarshaller(m_rootType);
    }

    public Marshaller<T> getRootMarshaller()
    {
        return createMarshaller(m_rootType);
    }

    public ClassModel getRootClassModel()
    {
        return getClassModel(m_rootType);
    }

    public T getRootInstance()
    {
        return m_rootInstance;
    }

    /**
     * Retrieves a ClassModel for a class. If it does not exist it is created. If the client tries to register
     * 2 classes with the same simple name, then the previously created ClassModel (and its managed instances)
     * will be removed.
     *
     * @param aClass
     * @return
     */
    public <E> ClassModel<E> getClassModel(Class<E> aClass)
    {
        ClassModel classModel = m_classModelMap.get(aClass);
        if (classModel == null)
        {
            classModel = ClassModelFactory.createClassModel(this, aClass);
            m_classModelMap.put(aClass, classModel);
            ClassModel cm = m_classModelBySimpleClassNameMap.put(aClass.getSimpleName(), classModel);
            if (cm != null)
            {
                System.out.println("WARNING: 2 classes registered with simple name " + aClass.getSimpleName());
            }
        }
        return classModel;
    }

    /**
     * Utility method for returning a ClassModel by its simple name. E.g. simple name of foo.bar.Person is Person.
     * Simple names are widely used in the framework for convenience, although it means that simple names of classes
     * must be unique within the domain model
     *
     * @param className
     * @return
     */
    public ClassModel getClassModelBySimpleName(String className)
    {
        return m_classModelBySimpleClassNameMap.get(className);
    }

    /**
     * Utility method for converting a list of classes to their classModels
     *
     * @param classes
     * @return
     */
    public List<ClassModel> getClassModels(Class[] classes)
    {
        ArrayList<ClassModel> cms = new ArrayList<ClassModel>();
        for (Class aClass : classes)
        {
            cms.add(getClassModel(aClass));
        }
        return cms;
    }

    /**
     * Maps the given class to special serialization mechanism. That means that when instances of this type
     * are read or written, the given StringSerializer is used, instead of traversing the class's
     * ClassModel. Also, when GUIs are generated for this class, a simple textfield can be used instead of
     * a specific GUI for the class.
     *
     * @param aClass
     * @param ss
     */
    public void addStringSerializerMapping(Class aClass, StringSerializer ss)
    {
        m_ssMap.put(aClass, ss);
    }

    public StringSerializer getStringSerializer(Class aClass)
    {
        return m_ssMap.get(aClass);
    }

    /**
     * Store the url of the included resource. XML resources can be included in djwastor XML files. The target XML
     * file to unmarshal has an element with the "djw-include" tag, which contains the URL of the resource to include
     *
     * @param unmarshalledObj
     * @param resourceURL
     */
    public void mapIncludedResourceURL(Object unmarshalledObj, String resourceURL)
    {
        m_resourceMap.put(unmarshalledObj, resourceURL);
    }

    /**
     * This method maps a resource to a reference to the object rather than the object itself. This means that
     * the object can be switched at runtime, but the resource URL will still be found at marshalling time
     * @param propertyObjectPair encapsulate the reference to the resource
     * @param resourceURL
     */
    public void mapIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair, String resourceURL)
    {
        m_resourceMapByReference.put(propertyObjectPair, resourceURL);
    }

    public String getIncludedResourceURL(Object unmarshalledObject)
    {
        return m_resourceMap.get(unmarshalledObject);
    }

    public String getIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair)
    {
        return m_resourceMapByReference.get(propertyObjectPair);
    }

    /**
     * Clears the classmodels currently managed by this Manager, whilst keeping other information such as
     * the string serialisation registry
     */
    public void reset()
    {
        m_classModelMap.clear();
        m_classModelBySimpleClassNameMap.clear();
        //m_idMap.clear(); //not yet used
        //m_instanceMap.clear(); //not yet used
        m_resourceMap.clear();
        m_resourceMapByReference.clear();
        clearKeyChangeHistory();
    }

    public boolean isSimpleType(Class aClass)
    {
        return aClass.isPrimitive() || m_simpleTypes.contains(aClass)
                || aClass.getAnnotation(SimpleType.class) != null;
    }

    /**
     * Creates a "child" class model manager. If a property is annotated with {@link @NewNamespace} then when the
     * unmarshaller comes across it it will create a new ClassModelManager for this branch of the XML tree. This
     * means that the same XML file can contain data that has separate namespaces (NOTE this has nothing to do with
     * XML namespaces!).
     *
     * @param retainList The retainlist allows the application to specify which classes it would like to share with all
     *                   its sub data models. For example an application that manages "mini applications" that work on different instances
     *                   of the same datamodel is allowed to share certain data between all of them, such as reference data.
     * @return
     */
    public ClassModelManager createChildCMM(Class[] retainList)
    {
        ClassModelManager cmm = new ClassModelManager(m_rootType, m_inspectionType);
        cmm.m_ssMap = (HashMap<Class, StringSerializer>) m_ssMap.clone();
        cmm.m_resourceMap = (HashMap<Object, String>) m_resourceMap.clone();
        for (Class cl : retainList)
        {
            ClassModel retainCM = getClassModel(cl);
            List<ClassModel> list = retainCM.getClassModelHeirarchy();
            for (ClassModel classModel : list)
            {
                cmm.m_classModelMap.put(classModel.getContainedClass(), classModel);
            }
        }
        return cmm;

    }

    public KeyChangeHistory getKeyChangeHistory()
    {
        return m_keyChangeHistory;
    }

    public void setInitialized(T obj)
    {
        m_initializing = false;
        m_rootInstance = obj;
    }

    /**
     * Utility method for creating an unmarshaller tied to this ClassModelManager
     *
     * @param aClass
     * @return
     */
    public <E> Unmarshaller<E> createUnmarshaller(Class<E> aClass)
    {
        return new Unmarshaller<E>(getClassModel(aClass));
    }

    /**
     * Utility method for creating a marshaller tied to this ClassModelManager
     *
     * @param aClass
     * @return
     */
    public Marshaller createMarshaller(Class aClass)
    {
        return new Marshaller(aClass, this, true);
    }

    /**
     * Utility method for creating an unmarshaller tied to this ClassModelManager by a class's fully qualified name,
     * e.g. "foo.bar.Person".
     *
     * @param className
     * @return
     */
    public Unmarshaller createUnmarshaller(String className)
    {
        return new Unmarshaller(getClassModelBySimpleName(className));
    }

    /**
     * Utility method for resolving a ClassModel by its fully qualified name, e.g. "foo.bar.Person"
     *
     * @param propertyClass
     * @return
     */
    public ClassModel getClassModelByName(String propertyClass)
    {
        try
        {
            return getClassModel(Class.forName(propertyClass));
        }
        catch (ClassNotFoundException e)
        {
            throw new XappException(e);
        }
    }

    public <T> T newInstance(Class<T> aClass)
    {
        return getClassModel(aClass).newInstance(null);
    }

    public MarshallingContext getMarshallerContext()
    {
        return this;
    }

    public ClassModelContext getClassModelContext()
    {
        return this;
    }

    public List<ClassModel> enumerateClassModels()
    {
        return new ArrayList<ClassModel>(m_classModelMap.values());
    }

    public Object getSingleton(Class aClass)
    {
        return getClassModel(aClass).getSingleton();

    }

    public boolean hasClassModel(Class aClass)
    {
        return m_classModelMap.containsKey(aClass);
    }

    public void clearKeyChangeHistory()
    {
        m_keyChangeHistory = new KeyChangeHistoryImpl();

    }

    public List<ClassModel> getClassModels()
    {
        return new ArrayList<ClassModel>(m_classModelMap.values());
    }

    public <E> E getInstanceNoCheck(Class<E> aClass, String key)
    {
        return getClassModel(aClass).getInstanceNoCheck(key);
    }

    public <E> E getInstance(Class<E> aClass, String key)
    {
        return getClassModel(aClass).getInstance(key);
    }

    public InspectionType getInspectionType()
    {
        return m_inspectionType;
    }


    public void registerInstance(ClassModel classModel, Object instance)
    {

    }

    public KeyChangeDictionary getKeyChangeDictionary()
    {
        return m_keyChangeHistory.getCurrentKeyChangeDictionary();
    }

    public Object resolveInstance(ClassModel classModel, String key)
    {
        if (!m_classModelMap.containsValue(classModel))
        {
            throw new XappException("classModel: " + classModel + " not managed by this Class Database");
        }
        Object o = classModel.getInstanceNoCheck(key);
        if (o == null)
        {
            System.out.println("object with key " + key + " of class " + classModel + " not found. Looking at key change history...");
            String newKey = m_keyChangeHistory.resolveKey(classModel.getSimpleName(), key);
            System.out.println("key resolved to " + newKey);
            o = classModel.getInstanceNoCheck(newKey);
            if(o==null)
            {
                System.out.println("WARNING: could not resolve object with key: "+newKey);
            }
        }
        return o;
    }

    public boolean isInitializing()
    {
        return m_initializing;
    }
}
