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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class MethodProperty extends AbstractPropertyAccess
{
    private final String m_name;
    private final Method m_accessor;
    private final Method m_modifier;

    public MethodProperty(Method accessor)
    {
        m_accessor = accessor;
        String name = accessor.getName();
        name = name.startsWith("is") ? name.substring(2) : name.substring(3);
        m_name = name;
        Method modifier = null;
        try
        {
            modifier = accessor.getDeclaringClass().getMethod("set" + name, new Class[]{accessor.getReturnType()});
        }
        catch (NoSuchMethodException e)
        {
            //class is read-only
        }
        m_modifier = modifier;
    }

    public void set(Object target, Object value) throws InvocationTargetException, IllegalAccessException
    {
        if (m_modifier!=null) {
            m_modifier.invoke(target, value);
        }
    }

    public Object get(Object target) throws InvocationTargetException, IllegalAccessException
    {
        return m_accessor.invoke(target);
    }

    public boolean isReadOnly()
    {
        return m_modifier==null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return m_accessor.getAnnotation(annotationClass);
    }

    public Type getGenericType()
    {
        return m_accessor.getGenericReturnType();
    }

    public String getName()
    {
        return m_name;
    }

    public Class getType()
    {
        return m_accessor.getReturnType();
    }

    public boolean isTransient()
    {
        return getAnnotation(Transient.class)!=null;
    }

    public boolean displayNodes()
    {
        return getAnnotation(Transient.class).displayNodes();
    }

    @Override
    public Class getDeclaringClass() {
        return m_accessor.getDeclaringClass();
    }

    public String toString()
    {
        return m_accessor.toString();
    }
}
