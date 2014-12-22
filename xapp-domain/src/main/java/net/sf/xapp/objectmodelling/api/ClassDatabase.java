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
package net.sf.xapp.objectmodelling.api;

import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;

import java.util.Collection;
import java.util.List;

/**
 * The ClassDatabase allows access to all the "managed objects" in the application instance. Managed objects
 * are those that have either been unmarshalled through {@link Unmarshaller} or that have been created via the
 * Application GUI.
 *
 */
public interface ClassDatabase<T>
{
    Unmarshaller<T> getRootUnmarshaller();
    Marshaller<T> getRootMarshaller();
    ClassModel<T> getRootClassModel();
    T getRootInstance();
    ObjectMeta<T> getRootObjMeta();

    <E> ClassModel<E> getClassModel(Class<E> aClass);

    ClassModel getClassModelBySimpleName(String className);

    ClassModel getClassModelByName(String propertyClass);

    List<ClassModel> getClassModels(Class[] classes);

    void addStringSerializerMapping(Class aClass, StringSerializer ss);

    StringSerializer getStringSerializer(Class aClass);

    void reset();

    boolean isSimpleType(Class aClass);

    <E> Unmarshaller<E> createUnmarshaller(Class<E> aClass);

    <E> Marshaller<E> createMarshaller(Class<E> aClass);

    Unmarshaller createUnmarshaller(String className);


    MarshallingContext getMarshallerContext();

    ClassModelContext getClassModelContext();

    List<ClassModel> enumerateClassModels();


    boolean hasClassModel(Class aClass);
    
    void clearKeyChangeHistory();

    List<ClassModel> getClassModels();

    <E> E getInstance(Class<E> aClass, String key);
    <E> E getInstanceNoCheck(Class<E> aClass, String key);

    InspectionType getInspectionType();
    void setMarshalDatesAsLongs();

    void registerInstance(ObjectMeta objectMeta);

    Collection<ObjectMeta> allManagedObjects();

    ObjectMeta findOrCreateObjMeta(ObjectLocation objectLocation, Object value);
    ObjectMeta find(Object value);

    void removeInstance(ObjectMeta objectMeta);

    <T> ObjectMeta<T> findObjById(Long objId);

    Long nextId();

    boolean isMaster();

    void setMaster(long startId);
    void setMaster(boolean master);

    void incrementRevision();

    Long getRev(); //TODO why does the class database track the rev, this is just a copy of the rev of the root object

    void setRevision(Long rev);

    long peekNextId();
}
