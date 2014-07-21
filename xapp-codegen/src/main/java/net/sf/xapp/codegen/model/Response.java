/*
 *
 * Date: 2010-jun-16
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

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

    public Message createMessage(Field principalField)
    {
        Message m = new ResponseMessage();
        if(principalField!=null)
        {
            m.getFields().add(principalField);
        }
        m.setName(getName());
        m.getFields().addAll(getFields());
        m.setModule(getModule());
        return m;
    }
}
