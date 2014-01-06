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

import net.sf.xapp.objectmodelling.core.ClassModel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;


public class ClassUtils
{
    /**
     *
     * @param classes
     * @return The "lowest" common class, Object.class if not in same heirarchy
     */
    public static Class getCommonClass(List<Class> classes)
    {
        if(classes.isEmpty())return null;
        Class commonClass = classes.get(0);
        if(classes.size()==1)
        {
            return commonClass;
        }
        for (int i = 1; i < classes.size(); i++)
        {
            Class c = classes.get(i);
            commonClass = getCommonClass(commonClass, c);
        }
        return commonClass;
    }

    public static ArrayList<Class> createClassList(List<Object> objects)
    {
        ArrayList<Class> classList = new ArrayList<Class>();

        for (Object ob : objects)
        {
            classList.add(ob.getClass());
        }
        return classList;
    }

    public static Class getCommonClass(Class classA, Class classB)
    {
        while(!classA.isAssignableFrom(classB))
        {
            classA=classA.getSuperclass();
        }
        return classA;
    }

    public static boolean hasAnnotationInHeirarchy(Class annotation, Class aClass)
    {
        return getAnnotationInHeirarchy(annotation, aClass)!=null;
    }
    public static Annotation getAnnotationInHeirarchy(Class annotationClass, Class aClass)
    {
        if(aClass.equals(Object.class)) return null;
        if(aClass.getAnnotation(annotationClass)!=null)
        {
            return aClass.getAnnotation(annotationClass);
        }
        if(aClass.getSuperclass()!=null)
        {
            Annotation annotation = getAnnotationInHeirarchy(annotationClass, aClass.getSuperclass());
            if (annotation!=null)
            {
                return annotation;
            }
        }
        for (Class anInterface : aClass.getInterfaces())
        {
            Annotation annotation = getAnnotationInHeirarchy(annotationClass, anInterface);
            if(annotation!=null)
            {
                return annotation;
            }
        }
        return null;
    }

    public static String toHeirarchyString(ClassModel cm)
    {
        return toHeirarchyString(cm.getContainedClass());
    }
    public static String toHeirarchyString(Class c)
    {
        StringBuilder sb = new StringBuilder();
        List<String> args = new ArrayList<String>();
        while(c!=Object.class && c!=null)
        {
            args.add(c.getSimpleName());
            c = c.getSuperclass();
        }
        for (int i = args.size()-1; i >=0 ; i--)
        {
            String s = args.get(i);
            sb.append(s);
            if(i>0)sb.append("<--");
        }
        return sb.toString();

    }
}
