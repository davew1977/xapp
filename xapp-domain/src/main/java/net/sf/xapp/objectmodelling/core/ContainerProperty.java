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

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.objectmodelling.ContainsReferences;
import net.sf.xapp.marshalling.stringserializers.StringMapSerializer;
import net.sf.xapp.objectmodelling.api.Rights;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.utils.XappException;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public class ContainerProperty extends Property
{
    protected Type mapValueType;
    protected Class mapKeyType;
    private List<Rights> m_restrictedRights;

    public ContainerProperty(ClassModelManager classModelManager, PropertyAccess propertyAccess, Class aClass,
                             Type mapValueType, Class<?> keyType, EditorWidget editorWidget,
                             Class parentClass)
    {
        super(classModelManager, propertyAccess, aClass, null, null, null/*map cannot be primary key*/, editorWidget, false, parentClass, "", true,false);
        this.mapValueType = mapValueType;
        this.mapKeyType = keyType;
        m_restrictedRights = new ArrayList<Rights>();
    }

    public boolean isAllowed(Rights... rights)
    {
        for (Rights right : rights)
        {
            if(m_restrictedRights.contains(right)) return false;
        }
        return true;
    }


    public void restrict(Rights... rights)
    {
        m_restrictedRights.addAll(Arrays.asList(rights));
    }

    public Class getContainedType()
    {
        if(mapValueType instanceof Class) {
            return (Class) mapValueType;
        } else   {
            ParameterizedType parameterizedType = (ParameterizedType) this.mapValueType;
            return (Class) parameterizedType.getRawType();
        }
    }

    public ClassModel getContainedTypeClassModel()
    {
        return classDatabase.getClassModel(getContainedType());
    }

    public Object get(Object target)
    {
        Object map = super.get(target);
        if(map==null || isEmpty(map)) {
            map = createCollection();
            try {
                propertyAccess.set(target, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    protected boolean isEmpty(Object map) {
        return ((Map)map).isEmpty();
    }

    @Override
    public Object convert(ObjectMeta objectMeta, String value) {
        if(isStringSerializable()) {
            return StringMapSerializer._read(mapKeyType, mapValueType, value);
        }
        throw new XappException(getName() + " map property is not string serializable");
    }

    @Override
    public String convert(ObjectMeta objectMeta, Object obj) {
        if(isStringSerializable()) {
            return StringMapSerializer._write((Map<?, ?>) obj);
        }
        throw new XappException(getName() + " map property is not string serializable");
    }

    @Override
    public Class getMainType() {
        return getContainedType();
    }

    @Override
    public String toString() {
        return String.format("%s<%s> %s", getPropertyClass().getSimpleName(), getContainedType().getSimpleName(), StringUtils.decapitaliseFirst(getName()));
    }

    @Override
    public boolean isComplexNonReference() {
        return !isTransient() && !isImmutable() && !containsReferences();
    }

    public final Object createCollection() {
        if(m_class.isInterface() || Modifier.isAbstract(m_class.getModifiers())) {
            return createDefaultCollection();
        }
        else return ReflectionUtils.newInstance(m_class);
    }

    public Object createDefaultCollection() {
        return new LinkedHashMap();
    }
    public Map map(Object container) {
        return (Map) get(container);
    }

    public boolean containsReferences()
    {
        return getContainsRefsAnnotation() !=null;
    }

    protected ContainsReferences getContainsRefsAnnotation()
    {
        return propertyAccess.getAnnotation(ContainsReferences.class);
    }

    public boolean contains(Object container, ObjectMeta objMeta) {
        return map(container).containsValue(objMeta.getInstance());
    }

    public Object addToMapOrCollection(Object mapOrCollection, int index, Object instance) {
        Object key = getContainedTypeClassModel().getKey(instance);
        return ((Map) mapOrCollection).put(key, instance);
    }
    public boolean removeFromMapOrCollection(Object mapOrCollection, Object instance) {
        Object key = getContainedTypeClassModel().getKey(instance);
        return ((Map) mapOrCollection).remove(key) != null;
    }

    public final ContainerAdd add(ObjectMeta container, int index, ObjectMeta instance) {
        Object result = addToMapOrCollection(get(container.getInstance()), index, instance.getInstance());
        return new ContainerAdd(this, container.getInstance(), result);
    }

    public final boolean remove(ObjectMeta container, ObjectMeta instance) {
        return removeFromMapOrCollection(get(container.getInstance()), instance.getInstance());
    }

    public Collection getCollection(Object listOwner) {
        return map(listOwner).values();
    }

    public int indexOf(ObjectMeta container, ObjectMeta objectMeta) {
        if(isList()) {
            return ((List) get(container.getInstance())).indexOf(objectMeta.getInstance());
        } else {
            return -1;
            //throw new UnsupportedOperationException("Cannot call indexOf unless list " + this);
        }
    }

    public int size(ObjectMeta obj) {
        return map(obj.getInstance()).size();
    }

    @Override
    public void eachValue(ObjectMeta target, PropertyValueIterator propertyValueIterator) {
        Collection collection = new ArrayList(getCollection(target.getInstance()));
        int index=0;
        for (Object o : collection) {
            if (o!=null) {
                propertyValueIterator.exec(new ObjectLocation(target, this), index++, o);
            }
        }
    }

    public boolean isContainerListProperty() {
        return getClassDatabase().getClassModel(getParentClass()).getContainerProperty() == this;
    }

    public Class getMapKeyType() {
        return mapKeyType;
    }

    @Override
    public boolean isStringSerializable() {
        Class containedType = getContainedType();
        return containedType.equals(String.class) || containedType.isPrimitive() || containedType.isEnum() ||
                Number.class.isAssignableFrom(containedType) || containedType.equals(Boolean.class) ||
                containedType.equals(Character.class) ||
                Collection.class.isAssignableFrom(containedType);
    }

    public Type getMapValueType() {
        return mapValueType;
    }

    public static void main(String[] args) {
        System.out.println(Integer.class.isPrimitive());
    }
}
