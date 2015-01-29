/*
 *
 * Date: 2009-nov-30
 * Author: davidw
 *
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.marshalling.PropertyOrder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

public abstract class AbstractPropertyAccess<T extends AccessibleObject> implements PropertyAccess {
    private T prop;
    private int order = 0;

    public AbstractPropertyAccess(T prop) {
        this.prop = prop;
        prop.setAccessible(true);
        PropertyOrder propOrder = getAnnotation(PropertyOrder.class);
        if(propOrder !=null)
        {
            order =  propOrder.value();
        }
    }

    public final int getOrdering() {
        return order;
    }

    @Override
    public void setOrdering(int i) {
        this.order = i;
    }

    public T getProp() {
        return prop;
    }

    public final <E extends Annotation> E getAnnotation(Class<E> annotationClass)
    {
        return prop.getAnnotation(annotationClass);
    }
    public final String toString()
    {
        return prop.toString();
    }

}
