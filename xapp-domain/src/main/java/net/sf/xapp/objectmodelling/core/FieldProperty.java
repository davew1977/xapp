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

import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;


public class FieldProperty extends AbstractPropertyAccess
{
    private final Field m_field;
    private String m_name;

    public FieldProperty(Field field)
    {
        m_field = field;
        m_field.setAccessible(true);
        String name = field.getName();
        name = name.startsWith("m_") ? name.substring(2) : name;
        name = StringUtils.capitalizeFirst(name);
        m_name = name;
    }

    public void set(Object target, Object value) throws InvocationTargetException, IllegalAccessException
    {
        m_field.set(target, value);
    }

    public Object get(Object target) throws InvocationTargetException, IllegalAccessException
    {
        return m_field.get(target);
    }

    public boolean isReadOnly()
    {
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return m_field.getAnnotation(annotationClass);
    }

    public Type getGenericType()
    {
        return m_field.getGenericType();
    }

    public String getName()
    {
        return m_name;
    }

    public Class getType()
    {
        return m_field.getType();
    }

    public boolean isTransient()
    {
        return getAnnotation(Transient.class)!=null;
    }

    public boolean displayNodes()
    {
        return getAnnotation(Transient.class)!=null && getAnnotation(Transient.class).displayNodes();
    }

    @Override
    public Class getDeclaringClass() {
        return m_field.getDeclaringClass();
    }

    public String toString()
    {
        return m_field.toString();
    }
}