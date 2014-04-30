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
import net.sf.xapp.objectmodelling.api.Rights;

import java.util.*;


public class ContainerProperty extends Property
{
    protected Class m_containedType;
    private List<Rights> m_restrictedRights;

    public ContainerProperty(ClassModelManager classModelManager, PropertyAccess propertyAccess, Class aClass,
                             Class containedType, EditorWidget editorWidget,
                             Class parentClass)
    {
        super(classModelManager, propertyAccess, aClass, null, null, null/*map cannot be primary key*/, null, editorWidget, false, parentClass, "", true,null, false);
        m_containedType = containedType;
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
        return m_containedType;
    }

    public ClassModel getContainedTypeClassModel()
    {
        return m_classDatabase.getClassModel(m_containedType);
    }

    public Object get(Object target)
    {
        Object map = super.get(target);
        if(map==null) {
            map = createCollection();
            try {
                m_propertyAccess.set(target, map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
    public Object createCollection() {
        return new LinkedHashMap();
    }
    private Map map(Object container) {
        return (Map) get(container);
    }

    public boolean containsReferences()
    {
        return getContainsRefsAnnotation() !=null;
    }

    protected ContainsReferences getContainsRefsAnnotation()
    {
        return m_propertyAccess.getAnnotation(ContainsReferences.class);
    }

    public boolean contains(Object container, Object instance) {
        return map(container).containsValue(instance);
    }

    public void addToMapOrCollection(Object mapOrCollection, Object instance) {
        String key = getContainedTypeClassModel().getGlobalKey(instance);
        ((Map) mapOrCollection).put(key, instance);
    }

    public void add(Object container, Object instance) {
        addToMapOrCollection(map(container), instance);
    }

    public Collection getCollection(Object listOwner) {
        return map(listOwner).values();
    }
}
