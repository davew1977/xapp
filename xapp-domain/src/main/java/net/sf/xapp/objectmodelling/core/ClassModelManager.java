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
import net.sf.xapp.utils.ObjMetaNotFoundException;
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
    private Class rootType;
    private AtomicLong idSequence = new AtomicLong(0); //only used if the objects don't have an id already
    private Map<Long, ObjectMeta> instanceMap;
    private HashMap<Class, ClassModel> classModelMap;
    private HashMap<String, ClassModel> classModelBySimpleClassNameMap;//e.g. "Config" -> Config Class Model
    private HashMap<Class, StringSerializer> ssMap;
    private HashMap<Object, String> resourceMap;
    private HashMap<PropertyObjectPair, String> resourceMapByReference;
    private Map<Class, List<Class>> validSubTypes;  //config when model can't be annotated
    private Map<Class,String> keyProperties;
    private HashSet<Class> simpleTypes;
    private KeyChangeHistory keyChangeHistory;

    /**
     * this flag is false until the root unmarshaller has finished unmarshalling the root document
     */
    private boolean m_initializing;
    private ObjectMeta<T> rootObjMeta;
    private InspectionType m_inspectionType;
    private boolean master;

    public ClassModelManager(Class<T> rootType)
    {
        this(rootType, InspectionType.METHOD);
    }
    public <T> ClassModelManager(Class<T> rootType, InspectionType inspectionType)
    {
        m_inspectionType = inspectionType;
        instanceMap = new LinkedHashMap<Long, ObjectMeta>();
        this.rootType = rootType;
        classModelMap = new HashMap<Class, ClassModel>();
        classModelBySimpleClassNameMap = new HashMap<String, ClassModel>();
        ssMap = new HashMap<Class, StringSerializer>();
        resourceMap = new HashMap<Object, String>();
        resourceMapByReference = new HashMap<PropertyObjectPair, String>();
        validSubTypes = new HashMap<>();
        keyProperties = new HashMap<>();
        simpleTypes = new HashSet<Class>();
        simpleTypes.add(Integer.class);
        simpleTypes.add(Long.class);
        simpleTypes.add(Boolean.class);
        simpleTypes.add(Double.class);
        simpleTypes.add(Float.class);
        simpleTypes.add(Character.class);
        simpleTypes.add(Byte.class);
        simpleTypes.add(Short.class);
        simpleTypes.add(String.class);

        ssMap.put(String[].class, new StringArraySerializer());
        ssMap.put(int[].class, new IntegerArraySerializer());
        ssMap.put(long[].class, new LongListSerializer());
        ssMap.put(Date.class, new DateStringSerializer());
        ssMap.put(Class.class, new ClassStringSerializer());

        keyChangeHistory = new KeyChangeHistoryImpl();

        m_initializing = true;
    }

    public void setMarshalDatesAsLongs() {
        ssMap.put(Date.class, new DateLongSerializer());
    }

    @Override
    public void registerInstance(ObjectMeta objectMeta) {
        instanceMap.put(objectMeta.getId(), objectMeta);
    }

    @Override
    public Collection<ObjectMeta> allManagedObjects() {
        return instanceMap.values();
    }

    @Override
    public ObjectMeta findOrCreateObjMeta(ObjectLocation objectLocation, Object value) {
        ClassModel<?> classModel = getClassModel(value.getClass());
        return classModel.findOrCreate(objectLocation, value);
    }

    public ObjectMeta find(Object value) {
        ClassModel classModel = getClassModel(value.getClass());
        return classModel.find(value);
    }

    @Override
    public void removeInstance(ObjectMeta objectMeta) {
        instanceMap.remove(objectMeta.getId());
    }

    @Override
    public ObjectMeta findObjById(Long objId) {
        return instanceMap.get(objId);
    }

    @Override
    public Long nextId() {
        return idSequence.getAndIncrement();
    }

    @Override
    public boolean isMaster() {
        return master;
    }

    @Override
    public void setMaster(long startId) {
        master=true;
        idSequence.set(startId);
    }

    @Override
    public long peekNextId() {
        return idSequence.get();
    }

    @Override
    public Long getRev() {
        return getRootObjMeta().getRevision();
    }

    @Override
    public void setRev(Long rev) {
        getRootObjMeta().setRev(rev);
    }

    @Override
    public <E> void registerSubType(Class<E> parent, Class<? extends E>... childTypes) {
        List<Class> classes = validSubTypes.get(parent);
        if(classes == null) {
            classes = new ArrayList<>();
            validSubTypes.put(parent, classes);
        }
        for (Class childType : childTypes) {

            classes.add(childType);
        }
    }

    @Override
    public List<Class> getValidSubtypes(Class parent) {
        List<Class> classes = validSubTypes.get(parent);
        return classes != null ? classes : new ArrayList<Class>();
    }

    @Override
    public void setKeyProperty(Class aClass, String propName) {
        keyProperties.put(aClass, propName);
    }

    @Override
    public String getKeyProperty(Class aClass) {
        return keyProperties.get(aClass);
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public Unmarshaller getRootUnmarshaller()
    {
        return createUnmarshaller(rootType);
    }

    public Marshaller<T> getRootMarshaller()
    {
        return createMarshaller(rootType);
    }

    public ClassModel getRootClassModel()
    {
        return getClassModel(rootType);
    }

    public T getRootInstance()
    {
        return rootObjMeta.getInstance();
    }

    @Override
    public ObjectMeta<T> getRootObjMeta() {
        return rootObjMeta;
    }

    public void setRootObjMeta(ObjectMeta<T> rootObjMeta) {
        if (this.rootObjMeta==null) {
            this.rootObjMeta = rootObjMeta;
        }
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
        ClassModel classModel = classModelMap.get(aClass);
        if (classModel == null)
        {
            classModel = ClassModelFactory.createClassModel(this, aClass);
            classModelMap.put(aClass, classModel);
            ClassModel cm = classModelBySimpleClassNameMap.put(aClass.getSimpleName(), classModel);
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
        return classModelBySimpleClassNameMap.get(className);
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
        ssMap.put(aClass, ss);
    }

    public StringSerializer getStringSerializer(Class aClass)
    {
        return ssMap.get(aClass);
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
        resourceMap.put(unmarshalledObj, resourceURL);
    }

    /**
     * This method maps a resource to a reference to the object rather than the object itself. This means that
     * the object can be switched at runtime, but the resource URL will still be found at marshalling time
     * @param propertyObjectPair encapsulate the reference to the resource
     * @param resourceURL
     */
    public void mapIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair, String resourceURL)
    {
        resourceMapByReference.put(propertyObjectPair, resourceURL);
    }

    public String getIncludedResourceURL(Object unmarshalledObject)
    {
        return resourceMap.get(unmarshalledObject);
    }

    public String getIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair)
    {
        return resourceMapByReference.get(propertyObjectPair);
    }

    /**
     * Clears the classmodels currently managed by this Manager, whilst keeping other information such as
     * the string serialisation registry
     */
    public void reset()
    {
        classModelMap.clear();
        classModelBySimpleClassNameMap.clear();
        //m_idMap.clear(); //not yet used
        //m_instanceMap.clear(); //not yet used
        resourceMap.clear();
        resourceMapByReference.clear();
        clearKeyChangeHistory();
    }

    public boolean isSimpleType(Class aClass)
    {
        return aClass.isPrimitive() || simpleTypes.contains(aClass)
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
    /*public ClassModelManager createChildCMM(Class[] retainList)
    {
        ClassModelManager cmm = new ClassModelManager(rootType, m_inspectionType);
        cmm.ssMap = (HashMap<Class, StringSerializer>) ssMap.clone();
        cmm.resourceMap = (HashMap<Object, String>) resourceMap.clone();
        for (Class cl : retainList)
        {
            ClassModel retainCM = getClassModel(cl);
            Collection<ClassModel> list = retainCM.getClassModelHeirarchy();
            for (ClassModel classModel : list)
            {
                cmm.classModelMap.put(classModel.getContainedClass(), classModel);
            }
        }
        return cmm;

    }*/

    public KeyChangeHistory getKeyChangeHistory()
    {
        return keyChangeHistory;
    }

    public void setInitialized(ObjectMeta<T> obj)
    {
        m_initializing = false;
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
        return new ArrayList<ClassModel>(classModelMap.values());
    }

    public boolean hasClassModel(Class aClass)
    {
        return classModelMap.containsKey(aClass);
    }

    public void clearKeyChangeHistory()
    {
        keyChangeHistory = new KeyChangeHistoryImpl();

    }

    public List<ClassModel> getClassModels()
    {
        return new ArrayList<ClassModel>(classModelMap.values());
    }

    public <E> E getInstanceNoCheck(Class<E> aClass, String key)
    {
        try {
            return rootObjMeta.get(aClass, key);
        } catch (XappException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public <E> E getInstance(Class<E> aClass, String key)
    {
        return rootObjMeta.get(aClass, key);  //todo implement version that don't throw exception
    }

    public InspectionType getInspectionType()
    {
        return m_inspectionType;
    }


    public KeyChangeDictionary getKeyChangeDictionary()
    {
        return keyChangeHistory.getCurrentKeyChangeDictionary();
    }

    public Object resolveInstance(ClassModel classModel, String key)
    {
        throw new UnsupportedOperationException();
        /*if (!m_classModelMap.containsValue(classModel))
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
        return o;*/
    }

    public boolean isInitializing()
    {
        return m_initializing;
    }
}
