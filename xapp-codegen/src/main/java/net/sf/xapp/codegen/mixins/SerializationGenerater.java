/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.ComplexType;

public abstract class SerializationGenerater implements MixIn<ComplexType>
{
    public void mixIn(ComplexType obj, CodeFile ct)
    {
        init(obj, ct);
        genRead(obj, ct);
        genWrite(obj, ct);
    }

    abstract void init(ComplexType complexType, CodeFile ct);

    abstract void genRead(ComplexType complexType, CodeFile ct);

    abstract void genWrite(ComplexType complexType, CodeFile ct);
}