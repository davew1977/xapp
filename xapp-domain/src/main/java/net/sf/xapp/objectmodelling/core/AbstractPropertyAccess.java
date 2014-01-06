/*
 *
 * Date: 2009-nov-30
 * Author: davidw
 *
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.marshalling.PropertyOrder;

public abstract class AbstractPropertyAccess implements PropertyAccess
{
    public final int getOrdering()
    {
        PropertyOrder propOrder = getAnnotation(PropertyOrder.class);
        if(propOrder !=null)
        {
            return propOrder.value();
        }
        return 0;
    }
}
