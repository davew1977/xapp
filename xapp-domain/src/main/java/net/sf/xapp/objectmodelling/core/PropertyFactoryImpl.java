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
import net.sf.xapp.annotations.application.FilterOn;
import net.sf.xapp.annotations.application.Mandatory;
import net.sf.xapp.annotations.application.NotEditable;
import net.sf.xapp.annotations.marshalling.FormattedText;
import net.sf.xapp.annotations.objectmodelling.*;
import net.sf.xapp.utils.XappException;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;


public class PropertyFactoryImpl implements PropertyFactory
{
    public Property createProperty(ClassModelManager classModelManager, PropertyAccess propertyAccess, Class parentClass)
    {
        //look for bound property annotation
        EditorWidget editorWidgetAnnotation = propertyAccess.getAnnotation(EditorWidget.class);
        //transient?
        boolean editable = propertyAccess.getAnnotation(NotEditable.class) == null;

        Class aclass = propertyAccess.getType();
        if (Collection.class.isAssignableFrom(aclass))
        {
            Type returnType = propertyAccess.getGenericType();
            Class listType = null;
            if (returnType instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                Type type = parameterizedType.getActualTypeArguments()[0];
                if(type instanceof Class)
                {
                    listType = (Class) type;
                }
                else if(type instanceof ParameterizedType)
                {
                    ParameterizedType pt = (ParameterizedType) type;
                    listType = (Class) pt.getRawType();
                }
                else if(type instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) type;
                    Type[] bounds = typeVariable.getBounds();
                    listType = (Class) bounds[0];
                }
                else
                {
                    throw new XappException(propertyAccess.toString() + " has unhandled type info");
                }
            }
            else //rely on ListType annotation
            {
                ListType annotation = propertyAccess.getAnnotation(ListType.class);
                if (annotation == null)
                {
                    throw new XappException(propertyAccess.toString() + " not annotated with ListType annotation");
                }
                listType = annotation.value();
            }
            return new ListProperty(classModelManager, propertyAccess, aclass, listType, editorWidgetAnnotation, parentClass);
        } else if(Map.class.isAssignableFrom(aclass)) {
            ParameterizedType parameterizedType = (ParameterizedType) propertyAccess.getGenericType();
            //support only string as key
            Class<?> keyType = (Class) parameterizedType.getActualTypeArguments()[0];
            Type varType = parameterizedType.getActualTypeArguments()[1];

            //support only Strings, or enums as keys
            if(!propertyAccess.isTransient() && !keyType.equals(String.class) && !keyType.isEnum()) {
                throw new IllegalArgumentException(String.format("map properties must have string/enum keys %s", propertyAccess));
            }


            return new ContainerProperty(classModelManager, propertyAccess, aclass, varType, keyType, editorWidgetAnnotation, parentClass);
        }
        else
        {
            //look for reference referenceAnno
            Reference referenceAnno = propertyAccess.getAnnotation(Reference.class);
            //look for filter on referenceAnno
            FilterOn filterOn = propertyAccess.getAnnotation(FilterOn.class);
            //look for formatted text referenceAnno
            boolean formattedText = propertyAccess.getAnnotation(FormattedText.class) != null;
            boolean mandatory = propertyAccess.getAnnotation(Mandatory.class) != null;
            if (formattedText && !aclass.equals(String.class))
            {
                throw new XappException("formatted text referenceAnno can only apply to String");
            }
            //look for key
            Key key = propertyAccess.getAnnotation(Key.class);
            return new Property(classModelManager, propertyAccess, aclass,
                    referenceAnno, filterOn != null ? filterOn.value() : null,
                    key, editorWidgetAnnotation, formattedText,
                    parentClass, referenceAnno != null ? referenceAnno.select() : null, editable, mandatory);
        }
    }
}
