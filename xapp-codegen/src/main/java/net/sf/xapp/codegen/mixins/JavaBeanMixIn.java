/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.model.*;

import java.util.List;

import static java.lang.String.format;

public class JavaBeanMixIn implements MixIn<ComplexType>
{
    public void mixIn(ComplexType complexType, CodeFile ct)
    {

        ct.docLine(complexType.getClass().getSimpleName());
        
        if (!complexType.listTypes().isEmpty())
        {
            ct.addImport("java.util.*");
        }
        for (Type type : complexType.fieldTypes())
        {
            String alt = type.getPackageName();
            if (alt != null)
            {
                ct.addImport(alt + "." + type.getName());
            }
        }
        genConstructors(complexType, ct);
        genGettersAndSetters(complexType, ct);

        if (complexType.isAbstract())
        {
            ct.setAbstract();
            //ct._abstract().method("type", complexType.getName() + "TypeEnum");
        }

        if (complexType.hasSuper())
        {
            ComplexType superType = complexType.getSuperType();
            String superName = superType.getName();
            ct.setSuper(superName);
            if(!superType.getPackageName().equals(complexType.getPackageName()))
            {
                ct.addImport(superType.getPackageName() + "." + superType.getName());
            }
            /*if (superType.isAbstract())
            {
                ct.method("type", superName + "TypeEnum");
                ct.line("return %sTypeEnum.%s", superName, complexType.getName());
            }*/
        }
        else
        {
            ct.setSuper("AbstractObject");
        }


    }

    private void genGettersAndSetters(ComplexType complexType, CodeFile ct)
    {
        List<net.sf.xapp.codegen.model.Field> fields = complexType.resolveFields(false);
        for (net.sf.xapp.codegen.model.Field field : fields)
        {
            String typeName = GenHelper.declaredTypeName(field);
            String defaultValue = defaultValue(field);

            String fn = field.getName();
            if (field.isTransient())
            {
                ct._transient();
            }
            if (complexType.isAbstract() || complexType.hasSubTypes())
            {
                ct._protected();
            }

            if(field.isReference()) {
                String tn = field.typeName();
                ct.field(refType(field), fn, net.sf.xapp.application.utils.codegen.Access.PRIVATE, defaultValue);
                ct.method(field.accessorName(), tn);
                ct.line("return %s != null ? %s.get() : null", field.getName(), field.getName());
                ct.method(field.modifierName(), "void", format("%s %s", tn , fn));
                ct.line(genModifyRefLine(field));
            } else {
                ct.field(typeName, fn, access(complexType.model().isGenerateSetters(), field), defaultValue);

            }
        }
    }

    private net.sf.xapp.application.utils.codegen.Access access(boolean generateSetters, net.sf.xapp.codegen.model.Field field)
    {
        if(generateSetters)  {
            return net.sf.xapp.application.utils.codegen.Access.READ_WRITE;
        }
        net.sf.xapp.codegen.model.Access access = field.getAccess();
        switch (access)
        {
            case READ_ONLY:
                return net.sf.xapp.application.utils.codegen.Access.READ_ONLY;
            case READ_WRITE:
                return net.sf.xapp.application.utils.codegen.Access.READ_WRITE;
        }
        return null;
    }

    private String defaultValue(net.sf.xapp.codegen.model.Field field)
    {
        if(field.getDefaultValue()!=null)
        {
            return field.getDefaultValue();
        }
        if (field.isMap()) {
            return format("new LinkedHashMap<%s,%s>()", field.mapKeyType(), field.getType());
        }
        if (field.isSet()) {
            return format("new LinkedHashSet<%s>()", field.getType());
        }
        else if (field.isList())
        {
            return "new ArrayList<" + field.getType() + ">()";
        }
        else if (field.getType() instanceof PrimitiveType)
        {
            String tn = field.getType().getName();
            if (tn.equals("Long"))
            {
                return "0L";
            }
            else if (tn.equals("Integer"))
            {
                return "0";
            }
            else if (tn.equals("Boolean"))
            {
                return "false";
            }
        }
        else if (field.getType() instanceof EnumType)
        {
            EnumType enumType = (EnumType) field.getType();
            if (enumType.getDefaultValue() != null)
            {
                return enumType.getName() + "." + enumType.getDefaultValue();
            }
        }
        return null;
    }

    private void genConstructors(ComplexType complexType, CodeFile ct)
    {
        ct.constructor();
        //generate a cloning constructor
        List<net.sf.xapp.codegen.model.Field> fields = complexType.resolveFields(false);
        String varname = StringUtils.decapitaliseFirst(complexType.getName());
        ct.constructor(complexType.getName() + " " + varname);
        if(complexType.hasSuper())
        {
            ct.line("super(%s)", varname);
        }
        for (net.sf.xapp.codegen.model.Field field : fields)
        {
            String fn = field.getName();
            if (field.isMap()) {
                ct.line("this.%s = new LinkedHashMap<%s, %s>(%s.%s)", fn, field.mapKeyType(), field.getType(), varname, fn);
            }
            else if(field.isSet()) {
                ct.line("this.%s = new LinkedHashSet<%s>(%s.%s)", fn, field.getType(), varname, fn);
            }
            else if (field.isList())
            {
                ct.line("this.%s = new ArrayList<%s>(%s.%s)", fn, field.getType(), varname, fn);
            }
            else
            {
                ct.line("this.%s = %s.%s", fn, varname, fn);
            }
        }

        List<FieldSet> fieldSets = complexType.constructors();
        for (FieldSet fieldSet : fieldSets)
        {
            fields = fieldSet.getFields();
            if (!fields.isEmpty())
            {
                ct.constructor(GenHelper.paramListStr(fields));
                for (net.sf.xapp.codegen.model.Field field : fields)
                {
                    String fn = field.getName();
                    if(field.isReference()) {
                        ct.line(genModifyRefLine(field));
                    } else {
                        ct.line("this.%1$s = %1$s", fn);
                    }
                }
            }
        }
    }

    private String genModifyRefLine(net.sf.xapp.codegen.model.Field field) {
        String fn = field.getName();
        String tn = field.typeName();
        return format("this.%s = %s != null ? new %s(%s.class, %s) : null", fn, fn, refType(field), tn, fn);
    }

    private String refType(net.sf.xapp.codegen.model.Field field) {
        assert field.isReference();
        ComplexType complexType = (ComplexType) field.getType();
        return format("Ref<%s%s>", complexType.isAbstract() ? "? extends " : "", field.typeName());
    }

}