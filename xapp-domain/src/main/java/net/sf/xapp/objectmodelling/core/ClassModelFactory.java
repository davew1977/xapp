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

import net.sf.xapp.annotations.application.BoundObjectType;
import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.objectmodelling.PostInit;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.tree.Tree;
import net.sf.xapp.utils.ClassUtils;
import net.sf.xapp.utils.XappException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassModelFactory
{
    private static PropertyFactory m_propFactory = new PropertyFactoryImpl();

    public static ClassModel createClassModel(ClassModelManager classModelManager, Class aClass)
    {
        InspectionTuple inspectionTuple = createInspectionTuple(classModelManager, aClass);
        //is this a container?
        Container containerAnnotation = (Container) ClassUtils.getAnnotationInHeirarchy(Container.class, aClass);
        String containerListProp = containerAnnotation != null ? containerAnnotation.listProperty() : null;
        if (Tree.class.isAssignableFrom(aClass))
        {
            containerListProp = "children"; //hard code the container property for subtypes of Tree
        }
        ValidImplementations annotation = (ValidImplementations) aClass.getAnnotation(ValidImplementations.class);
        List<ClassModel> validImplementations = new ArrayList<ClassModel>();
        if (annotation != null)
        {
            Class[] vis = annotation.value();
            for (Class vi : vis)
            {
                if (aClass.equals(vi))
                    throw new XappException("class " + vi.getName() + " has itself as a valid implementation");

                validImplementations.add(classModelManager.getClassModel(vi));
            }
        }
        EditorWidget bcAnnotation = (EditorWidget) aClass.getAnnotation(EditorWidget.class);
        BoundObjectType boAnnotation = (BoundObjectType) aClass.getAnnotation(BoundObjectType.class);

        Collections.sort(inspectionTuple.properties);
        Collections.sort(inspectionTuple.listProperties);

        //find the primary key
        Property primaryKeyProperty = null;
        for (Property property : inspectionTuple.properties)
        {
            if (property.isPrimaryKey())
            {
                if (primaryKeyProperty != null)
                    throw new XappException("There can only be one primary key!: " + property + " " + primaryKeyProperty);
                primaryKeyProperty = property;
            }
        }
        ClassModel classModel = new ClassModel(classModelManager,
                aClass,
                inspectionTuple.properties,
                inspectionTuple.listProperties,
                inspectionTuple.mapProperties,
                validImplementations,
                bcAnnotation,
                boAnnotation,
                primaryKeyProperty,
                containerListProp,
                inspectionTuple.postInitMethod);
        return classModel;
    }

    private static InspectionTuple createInspectionTuple(ClassModelManager classModelManager, Class aClass)
    {
        Method[] methods = aClass.getMethods();
        InspectionTuple inspectionTuple = new InspectionTuple();
        InspectionType inspectionType = classModelManager.getInspectionType();
        for (Method method : methods)
        {
            if (inspectionType == InspectionType.METHOD && isAccessor(method))
            {
                MethodProperty propertyAccess = new MethodProperty(method);
                createProperty(classModelManager, aClass, inspectionTuple, propertyAccess);
            }
            if (method.getAnnotation(PostInit.class) != null)
            {
                inspectionTuple.postInitMethod = method;
            }
        }
        if(inspectionType== InspectionType.FIELD)
        {
            for (Field field : selectFields(aClass))
            {
                if(Modifier.isStatic(field.getModifiers()))
                {
                    continue;
                }
                PropertyAccess propertyAccess = new FieldProperty(field);
                createProperty(classModelManager, aClass, inspectionTuple, propertyAccess);
            }
        }
        return inspectionTuple;
    }

    protected static List<Field> selectFields(Class aClass)
    {
        ArrayList<Field> fields = new ArrayList<Field>();
        if(!aClass.equals(Object.class))
        {
            fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
            if(aClass.getSuperclass()!=null && !aClass.getSuperclass().equals(Object.class))
            {
                fields.addAll(selectFields(aClass.getSuperclass()));
            }
        }
        return fields;
    }

    private static void createProperty(ClassModelManager classModelManager, Class aClass, InspectionTuple inspectionTuple, PropertyAccess propertyAccess)
    {
        Property property = m_propFactory.createProperty(classModelManager, propertyAccess, aClass);
        inspectionTuple.addProperty(property);
    }

    private static boolean isAccessor(Method method)
    {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is")) &&
                !name.equals("getDeclaringClass") &&
                !name.equals("getClass") &&
                method.getParameterTypes().length == 0 &&
                !Modifier.isStatic(method.getModifiers()) /*&&
                !(method.getReturnType().isAssignableFrom(List.class) && method.getAnnotation(Transient.class)!=null)*/;
    }

    public static class InspectionTuple
    {
        List<Property> properties = new ArrayList<Property>();
        List<ListProperty> listProperties = new ArrayList<ListProperty>();
        List<ContainerProperty> mapProperties = new ArrayList<ContainerProperty>();
        Method postInitMethod = null;

        public void addProperty(Property property) {
            if (property instanceof ListProperty)
            {
                listProperties.add((ListProperty) property);
            } else if(property instanceof ContainerProperty) {
                mapProperties.add((ContainerProperty) property);
            }
            else
            {
                properties.add(property);
            }
        }
    }
}
