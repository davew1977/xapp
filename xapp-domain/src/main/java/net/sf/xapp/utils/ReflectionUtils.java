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
package net.sf.xapp.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import static java.lang.String.format;

public class ReflectionUtils
{
    public static <T> T call(Class targetClass, String methodName, Object... params) {
        try
        {
            Method method = findMatchingMethod(targetClass, methodName, params);
            method.setAccessible(true);
            return (T) method.invoke(null, params);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public static <T> T call(Object target, String methodName, Object... params)
    {
        try
        {
            Method method = findMatchingMethod(target.getClass(), methodName, params);
            method.setAccessible(true);
            return (T) method.invoke(target, params);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public static <T> T tryCall(Object target, String methodName, Object... params)
    {
        try
        {
            Method method = findMatchingMethod(target.getClass(), methodName, params);
            if(method == null) {
                return null;
            }
            method.setAccessible(true);
            return (T) method.invoke(target, params);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void checkMethodExists(Class cl, String method, Object... p)
    {
        Method matchingMethod = findMatchingMethod(cl, method, p);
        if(matchingMethod==null)
        {
            throw new RuntimeException(format("no matching method found, %s, %s, %s", cl, method, Arrays.asList(p)));
        }
    }

    private static <T> Class[] typeArgs(Object[] params)
    {
        Class[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++)
        {
            Object param = params[i];
            paramTypes[i] = param.getClass();
        }
        return paramTypes;
    }

    public static <T> T newInstance(Class<T> aClass, Object... args)
    {
        try
        {
            return aClass.getConstructor(typeArgs(args)).newInstance(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasMethodInHierarchy(Class aClass, String methodName, Class... parameterTypes) {

        try
        {
            Method declaredMethod = aClass.getMethod(methodName, parameterTypes);
            return !declaredMethod.getDeclaringClass().equals(Object.class);
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    public static Method findMatchingMethod(Class cl, String method, Object... p)
    {
        HashSet<Method> methods = new HashSet<Method>();

        methods.addAll(Arrays.asList(cl.getDeclaredMethods()));
        Method match = null;
        for (Method m : methods)
        {
            if(m.getName().equals(method) && m.getParameterTypes().length == p.length)
            {
                if(match==null)
                {
                    match = m;
                }
                else
                {
                    throw new RuntimeException("too many matches for method " + method + " " + Arrays.asList(p));
                }
            }
        }
        if(match==null && !cl.getSuperclass().equals(Object.class))
        {
            return findMatchingMethod(cl.getSuperclass(), method, p);
        }
        return match;
    }

    public static Class classForName(String className)
    {
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Class mostGenericClass(Class aClass) {
        while (aClass.getSuperclass()!=null && aClass.getSuperclass() != Object.class) {
            aClass = aClass.getSuperclass();
        }
        return aClass;
    }
}
