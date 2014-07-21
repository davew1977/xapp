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
import net.sf.xapp.codegen.model.Entity;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.FieldMatcher;

import java.util.List;

public class ClusteredObjMixIn implements MixIn<ComplexType>
{
    private final String ENTITY_CLASSNAME = "AbstractEntity";
    private String fwkPackageName;

    public ClusteredObjMixIn(String fwkPackageName)
    {
        this.fwkPackageName = fwkPackageName;
    }

    public void mixIn(ComplexType complexType, CodeFile ct)
    {
        if (complexType instanceof Entity)
        {
            ct.setSuper(ENTITY_CLASSNAME);
            ct.addImport(fwkPackageName + "." + ENTITY_CLASSNAME);
        }
        genLockUnlock(complexType, ct);

        //inject all accessors/modifiers with a checklocked call
        for (Field field : complexType.fields(FieldMatcher.WRITEABLE))
        {
            ct.getAccessor(field.getName()).lineAtStart("checkLockedForRead()");
            if (!field.isList())  //todo maps not handled
            {
                ct.getModifier(field.getName()).lineAtStart("checkMarkedForWrite()");
            }
        }
        //modify default constructor
        if (complexType instanceof Entity)
        {

            for (Field field : complexType.fields(FieldMatcher.LIST))
            {

            }

            /*for (Field field : complexType.getFields())
            {
                String fn = field.getName();

                if (field.isHideList())
                {
                    String cfn = StringUtils.capitalizeFirst(field.getName());
                    String mn = String.format("add%sTo%s", field.getType(), cfn);
                    ct.method(mn, "void", field.getType() + " obj");
                    ct.line("checkLockedForWrite()");
                    ct.line("m_%s.add(obj)", fn);
                    mn = String.format("remove%sFrom%s", field.getType(), cfn);
                    ct.method(mn, "void", field.getType() + " obj");
                    ct.line("checkLockedForWrite()");
                    ct.line("m_%s.remove(obj)", fn);
                    mn = String.format("count%s", cfn);
                    ct.method(mn, "int");
                    ct.line("checkLockedForRead()");
                    ct.line("return m_%s.size()", fn);
                    ct.method("get" + cfn, field.getType().getName(), "int index");
                    ct.line("checkLockedForRead()");
                    ct.line("return m_%s.get(index)", fn);
                }
            }*/
        }
    }

    private void genLockUnlock(ComplexType complexType, CodeFile ct)
    {
        //if we are not a value object generate propagating lock/unlock methods
        if (complexType instanceof Entity)
        {
            List<Field> fields = complexType.getFields();
            doLockingMethod(ct, fields, "lock", true);
            doLockingMethod(ct, fields, "unlock", false);
        }
    }

    private void doLockingMethod(CodeFile ct, List<Field> fields, String methodName, boolean lock)
    {
        String varname = lock ? "forWrite" : "";
        String typeParam = lock ? "boolean " + varname : "";
        ct.method(methodName, "void", typeParam);
        ct.line("super.%s(%s)", methodName, varname);
        for (Field field : fields)
        {
            if (field.getType() instanceof ComplexType)
            {
                ComplexType type = (ComplexType) field.getType();
                if (type instanceof Entity)
                {
                    if (field.isList())  //todo maps not handled
                    {
                        ct.startBlock("for(%s obj : m_%s)", ENTITY_CLASSNAME, field.getName());
                        ct.line("obj.%s(%s)", methodName, varname);
                        ct.endBlock();
                    }
                    else
                    {
                        ct.line("m_%s.%s(%s)", field.getName(), methodName, varname);
                    }
                }
            }
        }
    }
}