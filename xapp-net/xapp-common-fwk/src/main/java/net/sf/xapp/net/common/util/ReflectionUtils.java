/*
 *
 * Date: 2011-feb-07
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class ReflectionUtils
{

    public static Object call(Object target, String methodName, Object... params)
    {
        Method method = findMatchingMethod(target.getClass(), methodName, params.length);
        return call(target, method, params);
    }

    public static <T> T call(Object target, Method method, Object[] params)
    {
        try
        {
            /*if (!method.isAccessible())
            {
                method.setAccessible(true);
            }*/
            return (T) method.invoke(target, params);
        }
        catch (Exception e)
        {
            throw new RuntimeException(format("exception in method %s in object %s args are %s", method, target, Arrays.toString(params)), e);
        }
    }

    public static void checkMethodExists(Class cl, String method, Object... p)
    {
        Method matchingMethod = findMatchingMethod(cl, method, p.length);
        if (matchingMethod == null)
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

    public static <T> T newInstance(String className, Class[] types, Object... args)
    {
        try
        {
            Class<T> cl = (Class<T>) Class.forName(className);
            Constructor<T> constructor = cl.getConstructor(types);
            return constructor.newInstance(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> aClass, Class[] types, Object... args)
    {
        try
        {
            return aClass.getConstructor(types).newInstance(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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

    public static Method findMatchingMethod(Class cl, String method)
    {
        return findMatchingMethod(cl, method, -1);
    }

    public static Method findMatchingMethod(Class cl, String method, int noOfParams)
    {
        List<Method> methods = Arrays.asList(cl.getDeclaredMethods());
        Method match = null;
        for (Method m : methods)
        {
            boolean paramsOk = noOfParams == -1 || m.getParameterTypes().length == noOfParams;
            if (m.getName().equals(method) && paramsOk)
            {
                if (match == null)
                {
                    match = m;
                }
                else
                {
                    throw new RuntimeException("too many matches for method " + method + " with " + noOfParams + " params");
                }
            }
        }
        if (match == null && !cl.getSuperclass().equals(Object.class))
        {
            return findMatchingMethod(cl.getSuperclass(), method, noOfParams);
        }
        return match;
    }
}
