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

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;

import static java.lang.String.format;

public class ReflectionUtils {
    public static <T> T call(Class targetClass, String methodName, Object... params) {
        try {
            Method method = findMatchingMethod(targetClass, methodName, params);
            method.setAccessible(true);
            return (T) method.invoke(null, params);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T call(Object target, String methodName, Object... params) {
        try {
            Method method = findMatchingMethod(target.getClass(), methodName, params);
            method.setAccessible(true);
            return (T) method.invoke(target, params);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Object accessField(Class type, Object target, String fieldName) {
        Method method = findGetterMethod(type, fieldName);
        if (method != null) {
            return invokeMethod(target, method);
        }
        else {
            return getField(field(type, fieldName), target);
        }
    }

    public static void modifyField(String fieldName, Object target, Object valueToSet) {
        modifyField(target.getClass(), fieldName, target, valueToSet);
    }
    public static void modifyField(Class entityType, String fieldName, Object target, Object valueToSet) {
        Method method = findSetterMethod(entityType, fieldName);
        if (method != null) {
            invokeMethod(target, method, valueToSet);
            return;
        }
        setField(field(entityType, fieldName), target, valueToSet);
    }

    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        }
        catch (IllegalAccessException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException(
                    "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private static Object invokeMethod(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("target: %s method: %s args: %s", target, method, Arrays.toString(args)), e);
        }
    }

    public static void checkMethodExists(Class cl, String method, Object... p) {
        Method matchingMethod = findMatchingMethod(cl, method, p);
        if (matchingMethod == null) {
            throw new RuntimeException(format("no matching method found, %s, %s, %s", cl, method, Arrays.asList(p)));
        }
    }

    private static <T> Class[] typeArgs(Object[] params) {
        Class[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            paramTypes[i] = param.getClass();
        }
        return paramTypes;
    }

    public static <T> T newInstance(Class<T> aClass, Object... args) {
        try {
            return aClass.getConstructor(typeArgs(args)).newInstance(args);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasMethodInHierarchy(Class aClass, String methodName, Class... parameterTypes) {

        try {
            Method declaredMethod = aClass.getMethod(methodName, parameterTypes);
            return !declaredMethod.getDeclaringClass().equals(Object.class);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static Method findMatchingMethod(Class cl, String method, Object... p) {
        HashSet<Method> methods = new HashSet<Method>();

        methods.addAll(Arrays.asList(cl.getDeclaredMethods()));
        Method match = null;
        for (Method m : methods) {
            if (m.getName().equals(method) && m.getParameterTypes().length == p.length) {
                if (match == null) {
                    match = m;
                }
                else {
                    throw new RuntimeException("too many matches for method " + method + " " + Arrays.asList(p));
                }
            }
        }
        if (match == null && !cl.getSuperclass().equals(Object.class)) {
            return findMatchingMethod(cl.getSuperclass(), method, p);
        }
        return match;
    }

    public static Class classForName(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field field(Class<?> type, String fieldName) {
        Field field = findField(type, fieldName);
        makeAccessible(field);
        return field;
    }

    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    private static Method findGetterMethod(Class type, String fieldName) {
        Method method = findMethod(type, "get" + StringUtils.capitalizeFirst(fieldName));
        if(method == null) {
            method = findMethod(type, "is" + StringUtils.capitalizeFirst(fieldName));
        }
        return method;
    }

    private static Method findSetterMethod(Class type, String fieldName) {
        return findMethod(type, "set" + StringUtils.capitalizeFirst(fieldName), (Class[]) null);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                if (name.equals(method.getName()) &&
                        (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        }
        catch (IllegalAccessException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException(
                    "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }
    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }
}
