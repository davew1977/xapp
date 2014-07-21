/*
 *
 * Date: 2010-maj-03
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Message;

public class ToStringMixin implements MixIn<ComplexType>
{
    @Override
    public void mixIn(ComplexType complexType, CodeFile ct)
    {
        ct.method("toString", "String");
        if(complexType instanceof Message)
        {
            ct.line("return type() + \",\" + serialize()");
        }
        else if(complexType.getFields().size()==1)
        {
            ct.line("return String.valueOf(%s)", complexType.getFields().get(0).getName());
        }
        else
        {
            ct.line("return getClass().getSimpleName() + \" \" + serialize()");
        }
    }
}