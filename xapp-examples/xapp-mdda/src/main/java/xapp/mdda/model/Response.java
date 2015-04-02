/*
 *
 * Date: 2010-jun-16
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.application.Hide;
import net.sf.xapp.annotations.objectmodelling.Key;

public class Response extends ValueObject
{
    @Override
    @Hide
    @Key
    public String getName()
    {
        return super.getName();
    }
}
