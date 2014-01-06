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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public interface PropertyAccess
{
    void set(Object target, Object value) throws InvocationTargetException, IllegalAccessException;
    Object get(Object target) throws InvocationTargetException, IllegalAccessException;

    boolean isReadOnly();

    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    Type getGenericType();

    String getName();

    Class getType();

    /**
     *
     * @return true if the property should not be serialized
     */
    boolean isTransient();

    boolean displayNodes();

    int getOrdering();
}
