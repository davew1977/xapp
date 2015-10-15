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
import net.sf.xapp.marshalling.stringserializers.EnumListSerializer;
import net.sf.xapp.marshalling.stringserializers.IntegerListSerializer;
import net.sf.xapp.marshalling.stringserializers.LongListSerializer;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.utils.XappException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.List;


public class ListProperty extends ContainerProperty {

    public ListProperty(ClassModelManager classModelManager, PropertyAccess propertyAccess, Class aClass,
                        Class containedType, EditorWidget editorWidget,
                        Class parentClass) {
        super(classModelManager, propertyAccess, aClass, containedType, null, editorWidget, parentClass);
    }

    public Collection get(Object target) {
        return (Collection) super.get(target);
    }

    @Override
    protected boolean isEmpty(Object map) {
        return ((Collection)map).isEmpty();
    }

    public List castToList(Object target) {
        return (List) super.get(target); //note! will blow up if collection is set
    }


    @Override
    public Object convert(ObjectMeta target, String value) {
        Class containedType = getContainedType();
        if (containedType == Integer.class) {
            return IntegerListSerializer.doRead(value);
        } else if (containedType == Long.class) {
            return LongListSerializer.doRead(value);
        } else if (containedType == String.class) {
            return StringUtils.appendToCollection((Collection<String>) createCollection(), value);
        } else if (Enum.class.isAssignableFrom(containedType)) {
            return EnumListSerializer.doRead(value, containedType, (Collection<Enum>) createCollection());
        }
        throw new XappException(getName() + " list property is not string serializable");
    }

    @Override
    public String convert(ObjectMeta objectMeta, Object obj) {
        Class containedType = getContainedType();
        if (containedType == Integer.class) {
            return IntegerListSerializer.doWrite((List<Integer>) obj);
        } else if (containedType == Long.class) {
            return LongListSerializer.doWrite((List<Long>) obj);
        } else if (containedType == String.class) {
            return StringUtils.join((Collection<? extends Object>) obj, ",");
        } else if (Enum.class.isAssignableFrom(containedType)) {
            return EnumListSerializer.doWrite((Collection<? extends Enum>) obj);
        }
        throw new XappException(getName() + " list property is not string serializable");
    }

    @Override
    public Collection createDefaultCollection() {
        if (isList()) {
            return new ArrayList();
        }
	else if (isSortedSetCollection()) {
		return new TreeSet();
	}
	else if (isSetCollection()) {
            return new LinkedHashSet();
        } else throw new IllegalArgumentException("Collection of type " + m_class + " not supported");
    }

    @Override
    public boolean contains(Object container, ObjectMeta objectMeta) {
        return get(container).contains(objectMeta);
    }

    @Override
    public Object addToMapOrCollection(Object mapOrCollection, int index, Object instance) {
        if (index == -1) {
            return ((Collection) mapOrCollection).add(instance);
        } else {
            List list = (List) mapOrCollection;
            list.add(index > list.size() ? list.size() : index, instance);
            return true;
        }
    }

    @Override
    public boolean removeFromMapOrCollection(Object mapOrCollection, Object instance) {
        return ((Collection) mapOrCollection).remove(instance);
    }

    @Override
    public Collection getCollection(Object listOwner) {
        return get(listOwner);
    }

    @Override
    public int size(ObjectMeta obj) {
        return getCollection(obj.getInstance()).size();
    }
}
