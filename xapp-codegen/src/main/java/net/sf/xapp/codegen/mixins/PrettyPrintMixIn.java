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
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.Type;

import java.util.List;

public class PrettyPrintMixIn implements MixIn<ComplexType>
{
    private final String methodName;
    private static final String INDENT = "  ";

    public PrettyPrintMixIn(String methodName)
    {
        this.methodName = methodName;
    }

    public void mixIn(ComplexType complexType, CodeFile ct)
    {
        //ct.addImport(fwkPackageName + ".PrettyPrinter");
        //ct.addImplements("PrettyPrinter");
        ct.method(methodName, "String");
        ct.line("StringBuilder sb = new StringBuilder()");
        ct.line("%s(sb, \"%s\")", methodName, "");
        ct.line("return sb.toString()");
        ct.method(methodName, "void", "StringBuilder sb", "String indent");
        List<Field> fields = complexType.resolveFields(true);
        ct.line("sb.append(\"{\\n\")");
        ct.line("boolean isNull");
        for (int i = 0; i < fields.size(); i++)
        {
            Field field = fields.get(i);
            ct.line("sb.append(indent + \"%s\"+ \"%s: \")", INDENT, field.getName());
            ct.line("isNull = %s == null", field.getName());
            ct.startBlock("if(!isNull)");
            genWriteBlock(ct, field);
            ct.endBlock();
            ct.startBlock("else");
            ct.line("sb.append(\"null\")");
            ct.endBlock();
            ct.line("sb.append('\\n')");
        }
        ct.line("sb.append(indent + \"}\")");
    }

    private void genWriteBlock(CodeFile ct, Field field)
    {
        String fn = field.getName();
        Type ft = field.getType();
        boolean reference = field.isReference();
        if (field.isList())
        {
            ct.line("sb.append('[')");
            String varname = itemVarname(ft.getName());
            ct.startBlock("for(%s %s : %s)", ft.getName(), varname, fn);
            genWriteObj(ct, varname, ft, true, reference);
            ct.endBlock();
            ct.line("sb.append(']')");
        }
        else if (field.isMap())
        {
            ct.line("sb.append('[')");
            Type mapKeyType = field.mapKeyType();
            ct.startBlock("for(Map.Entry<%s, %s> entry : %s.entrySet())", mapKeyType, ft.getName(), fn);
            ct.line("%s key = entry.getKey()", mapKeyType);
            ct.line("%s value = entry.getValue()", ft);
            ct.line("sb.append(\"[ key = \")");
            genWriteObj(ct, "key", mapKeyType, true, false);
            ct.line("sb.append(\"[ value = \")");
            genWriteObj(ct, "value", ft, true, reference);
            ct.line("sb.append(']')");
            ct.endBlock();
            ct.line("sb.append(']')");
        }
        else
        {
            genWriteObj(ct, fn, ft, false, reference);
        }
    }

    private static String itemVarname(String lt)
    {
        return StringUtils.decapitaliseFirst(lt) + "Item";
    }

    private void genWriteObj(CodeFile ct, String varname, Type type, boolean inList, boolean reference)
    {
        if (type instanceof ComplexType)
        {
            ct.line("%s%s.%s(sb, indent + \"%s\")", varname, reference?".get()":"", methodName, INDENT);
        }
        else
        {
            ct.line("sb.append(%s)", varname);
            if(inList)
            {
                ct.line("sb.append(\", \")");
            }

        }
    }
}