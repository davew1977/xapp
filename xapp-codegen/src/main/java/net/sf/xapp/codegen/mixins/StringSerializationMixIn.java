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
import static java.lang.String.valueOf;

public class StringSerializationMixIn extends SerializationGenerater implements MixIn<ComplexType> {
    private final String utilPackageName;

    public StringSerializationMixIn(String utilPackageName) {
        this.utilPackageName = utilPackageName;
    }

    void init(ComplexType complexType, CodeFile ct) {
        //ct.addImport(m_fwkPackageName + ".StringSerializable");
        //ct.addImport(m_fwkPackageName + ".StringBuildable");
        ct.addImport("java.util.List");
        //ct.addImplements("StringSerializable");
        //ct.addImplements("StringBuildable");
        ct.addImport(utilPackageName + ".StringUtils");
    }

    public void genRead(ComplexType complexType, CodeFile ct) {
        ct.method("populateFrom", "void", "List<Object> _data");
        genCollectionHelperVars(complexType, ct, true);
        ct.line("boolean isNull");
        ct.line("Object _value");
        ct.line("List args"); //used for abtsract types
        List<Field> fields = complexType.resolveFields(true);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ct.line("_value = _data.get(%s)", valueOf(i));
            ct.line("isNull = _value.equals(\"\")");
            if (field.isMandatory()) {
                ct.startBlock("if(isNull)");
                ct.line("throw new RuntimeException(\"mandatory property %s in %s was null\")", field.getName(), complexType.getName());
                ct.endBlock();
                genReadBlock(ct, field);
            } else {
                ct.startBlock("if(!isNull)");
                genReadBlock(ct, field);
                ct.endBlock();
            }

        }
        ct.method("deserialize", complexType.getName(), "String str");
        ct.line("populateFrom(StringUtils.parse(str))");
        ct.line("return this");
    }

    public void genWrite(ComplexType complexType, CodeFile ct) {
        List<Field> fields = complexType.resolveFields(true);
        ct.method("writeString", "void", "StringBuilder sb");
        append(ct, "[");
        ct.line("boolean isNull");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ct.line("isNull = %s == null", field.getName());
            if (field.isMandatory()) {
                ct.startBlock("if(isNull)");
                ct.line("throw new RuntimeException(\"mandatory property %s in %s was null\")", field.getName(), complexType.getName());
                ct.endBlock();
                genWriteBlock(ct, field);
            } else {
                ct.startBlock("if(!isNull)");
                genWriteBlock(ct, field);
                ct.endBlock();
            }
            /*if (i < fields.size() - 1)
            {*/
            ct.line("sb.append(',')");
            /*}*/
        }
        append(ct, "]");

        ct.method("serialize", "String");
        ct.line("StringBuilder sb = new StringBuilder()");
        ct.line("writeString(sb)");
        ct.line("return sb.toString()");
    }

    private static void genWriteBlock(CodeFile ct, Field field) {
        String fn = field.getName();
        Type ft = field.getType();
        boolean reference = field.isReference();
        if (field.isList()) {
            append(ct, "[");
            String varname = itemVarname(fn);
            ct.startBlock("for(%s %s : %s)", ft, varname, fn);
            genWriteObj(ct, varname, ft, reference);
            append(ct, ",");
            ct.endBlock();
            append(ct, "]");
        } else if (field.isMap()) {
            append(ct, "[");
            Type mapKeyType = field.mapKeyType();
            ct.startBlock("for(Map.Entry<%s, %s> entry : %s.entrySet())", mapKeyType, ft.getName(), fn);
            ct.line("%s key = entry.getKey()", mapKeyType);
            ct.line("%s value = entry.getValue()", ft);
            append(ct, "[");
            genWriteObj(ct, "key", mapKeyType, false);
            append(ct, ",");
            genWriteObj(ct, "value", ft, reference);
            append(ct, "],");
            ct.endBlock();
            append(ct, "]");

        } else {
            genWriteObj(ct, fn, ft, reference);
        }
    }

    private static CodeFile append(CodeFile ct, String s) {
        return ct.line("sb.append(\"%s\")", s);
    }

    private static void genWriteObj(CodeFile ct, String varname, Type type, boolean reference) {
        String tn = type.getName();
        if (type instanceof ComplexType && reference) {
            ct.line("sb.append(%s)", varname);
        } else if (type instanceof ComplexType && ((ComplexType) type).isAbstract()) {
            append(ct, "[");
            ct.line("sb.append(%s.type()).append(\",\")", varname);
            ct.line("%s.writeString(sb)", varname);
            append(ct, "]");
        } else if (type instanceof ComplexType) {
            ct.line("%s.writeString(sb)", varname);
        } else if (type.getName().equals("String")) {
            ct.line("sb.append(StringUtils.escapeSpecialChars(%s))", varname);
        } else if (type.getName().equals("Class")) {
            ct.line("sb.append(%s.getName())", varname);
        } else {
            ct.line("sb.append(%s)", varname);
        }
    }


    public static void genCollectionHelperVars(ComplexType complexType, CodeFile ct, boolean stringSerialisation) {
        List<Field> collectionFields = complexType.fields(FieldMatcher.COLLECTION);
        for (Field field : collectionFields) {
            if (field.isTransient()) {
                continue;
            }
            if (field.isList()) {
                ct.line("%s %s", field.getType(), itemVarname(field.getName()));
            } else {
                assert field.isMap();
                ct.line("%s %s", field.getType(), mapValueVarname(field.getName()));
                Type mapKeyType = field.mapKeyType();
                ct.line("%s %s", mapKeyType, mapKeyVarname(field.getName()));

            }
        }
        if (!collectionFields.isEmpty()) {
            ct.line("int length");
            if (stringSerialisation) {
                ct.line("List list");
            }
        }
    }

    private static void genReadBlock(CodeFile ct, Field field) {
        String fn = field.getName();
        Type ft = field.getType();
        boolean reference = field.isReference();
        if (field.isList()) {
            ct.line("list = (List) _value");
            ct.line("length = list.size()");
            ct.line("%s = new %s<%s>(length)", fn, field.isSet() ? "LinkedHashSet" : "ArrayList", ft.getName());
            ct.startBlock("for(int i=0; i<length; i++)");
            ct.line("_value = list.get(i)");
            String varname = itemVarname(fn);
            genReadObj(ct, varname, ft, reference);
            ct.line("%1$s.add(%2$s)", fn, varname);
            ct.endBlock();
        } else if (field.isMap()) {
            ct.line("list = (List) _value");
            ct.line("length = list.size()");
            ct.line("%s = new LinkedHashMap<%s,%s>()", fn, field.mapKeyType(), ft);
            ct.startBlock("for(int i=0; i<length; i++)");
            ct.line("List _entry = (List) list.get(i)");
            ct.line("_value = _entry.get(0)");
            String keyvarname = mapKeyVarname(fn);
            genReadObj(ct, keyvarname, field.mapKeyType(), reference);
            ct.line("_value = _entry.get(1)");
            String valuevarname = mapValueVarname(fn);
            genReadObj(ct, valuevarname, ft, reference);
            ct.line("%s.put(%s, %s)", fn, keyvarname, valuevarname);
            ct.endBlock();

        } else {
            genReadObj(ct, fn, ft, reference);
        }
    }

    private static void genReadObj(CodeFile ct, String varname, Type type, boolean reference) {
        String tn = type.getName();
        if (type instanceof ComplexType && reference) {
            ct.line("%s = new Ref<%s>(%s.class, (String)_value)", varname, tn, tn);
        } else if (type instanceof ComplexType && ((ComplexType) type).isAbstract()) {
            ComplexType complexType = (ComplexType) type;
            Type baseType = complexType.baseType();
            ct.line("args = (List) _value");
            String dtn = StringUtils.decapitaliseFirst(tn);
            ct.line("String %sType = (String) args.get(0)", dtn);
            ct.line("%s = (%s) net.sf.xapp.Global.create(%sType)", varname, tn, dtn);
            ct.line("%s.populateFrom((List) args.get(1))", varname);
        } else if (type instanceof ComplexType) {
            ct.line("%s = new %s()", varname, tn);
            ct.line("%s.populateFrom((List) _value)", varname);
        } else {
            ct.line("%s = %s", varname, genParseFromString(type, "(String) _value"));
        }
    }

    public static String genParseFromString(Type type, Object var) {
        String tn = type.getName();
        if (type instanceof PrimitiveType) {
            if (tn.equals("String")) {
                return format("StringUtils.unescapeSpecialChars(%s)", var);
            } else if (tn.equals("Integer")) {
                return format("Integer.parseInt(%s)", var);
            } else if (tn.equals("Long")) {
                return format("Long.parseLong(%s)", var);
            } else if (tn.equals("Boolean")) {
                return format("Boolean.parseBoolean(%s)", var);
            } else if (tn.equals("Float")) {
                return format("Float.parseFloat(%s)", var);
            } else if (tn.equals("Character")) {
                return format("Character.parseCharacter(%s)", var);
            } else if (tn.equals("Double")) {
                return format("Double.parseDouble(%s)", var);
            } else if (tn.equals("Byte")) {
                return format("Byte.parseByte(%s)", var);
            } else if(tn.equals("Class")) {
                return format("net.sf.xapp.utils.ReflectionUtils.classForName(%s)", var);
            }
        } else if (tn.equals("ObjectType")) {
            return format("net.sf.xapp.Global.getObjectType(%s)", var);
        } else if (type instanceof EnumType) {
            return format("%s.valueOf(%s)", tn, var);
        }
        throw new IllegalArgumentException();
    }

    public static String itemVarname(String lt) {
        return StringUtils.decapitaliseFirst(lt) + "Item";
    }

    public static String mapKeyVarname(String lt) {
        return StringUtils.decapitaliseFirst(lt) + "Key";
    }

    public static String mapValueVarname(String lt) {
        return StringUtils.decapitaliseFirst(lt) + "Value";
    }
}