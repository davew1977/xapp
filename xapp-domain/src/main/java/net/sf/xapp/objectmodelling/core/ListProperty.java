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
import net.sf.xapp.utils.XappException;
import net.sf.xapp.utils.StringUtils;

import java.util.*;


public class ListProperty extends ContainerProperty
{

    public ListProperty(ClassModelManager classModelManager, PropertyAccess propertyAccess, Class aClass,
                        Class containedType, EditorWidget editorWidget,
                        Class parentClass, Class[] sharedInNamespace)
    {
        super(classModelManager, propertyAccess, aClass, containedType, editorWidget, parentClass, sharedInNamespace);
    }

    public Collection get(Object target)
    {
        return (Collection) super.get(target);
    }

    public List castToList(Object target)
    {
        return (List) super.get(target); //note! will blow up if collection is set
    }


    @Override
    public Object convert(String value)
    {
        if(m_containedType==Integer.class)
        {
            return IntegerListSerializer.doRead(value);
        }
        else if(m_containedType == Long.class)
        {
            return LongListSerializer.doRead(value);
        }
        else if(m_containedType==String.class)
        {
            return StringUtils.appendToCollection(createCollection(), value);
        }
        else if(Enum.class.isAssignableFrom(m_containedType))
        {
            return EnumListSerializer.doRead(value, m_containedType);
        }
        throw new XappException(getName() + " list property is not string serializable");
    }

    public Collection createCollection() {
        if(isList()) {
            return new ArrayList();
        }
        else if(isSetCollection()) {
            return new LinkedHashSet();
        }
        else throw new IllegalArgumentException("Collection of type "+m_class+" not supported");
    }

    @Override
    public boolean contains(Object container, Object instance) {
        return get(container).contains(instance);
    }

    @Override
    public void addToMapOrCollection(Object mapOrCollection, Object instance) {
        ((Collection) mapOrCollection).add(instance);
    }

    @Override
    public void add(Object container, Object instance) {
        addToMapOrCollection(get(container), instance);
    }

    @Override
    public Collection getCollection(Object listOwner) {
        return get(listOwner);
    }


}
