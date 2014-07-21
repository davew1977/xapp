/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Entity;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.Message;

public class ReferenceAwareMixin implements MixIn<ComplexType>
{
    public void mixIn(ComplexType complexType, CodeFile ct)
    {
        if(complexType instanceof Message)  {
            return;
        }
        boolean observable = complexType instanceof Entity && ((Entity) complexType).isObservable();
        if(observable) {
            if(complexType.containsRefs()) {
                ct.field("Lookup", "lookup", Access.PRIVATE, "new Lookup()");
                ct.method("lookup", "Lookup");
                ct.line("return lookup");
            }
            ct.method("init", "void");
            if (complexType.containsRefs()) {
                ct.line("this.lookup = new Lookup()");
                ct.line("registerEntities()");
            }
        } else {
            ct.method("init", "void", "Lookup lookup");
        }

        if(complexType.containsRefs()) {
            //ensure referenced items get added
            for (Field field : complexType.resolveFields(true)) {
                if(field.isReference() || field.containsReferences()) {
                    String var = field.loopStart(ct);
                    ct.line("%s.init(lookup)", var);
                    field.loopEnd(ct);
                }
            }
        }

        if(complexType.containsRefs()) {
            if(observable) {
                ct.method("registerEntities", "void");
            } else {
                ct.method("registerEntities", "void", "Lookup lookup");
            }
            for (Field field : complexType.resolveFields(true)) {
                if(field.containsEntities()) {
                    String var = field.loopStart(ct);
                    if (field.isEntityDeclaration()) {
                        ct.line("lookup.add(%s.class, %s.getKey(), %s)", field.typeName(), var, var);
                    } else {
                        ct.line("%s.registerEntities(lookup)", var);
                    }
                    field.loopEnd(ct);
                }
            }
        }
    }
}