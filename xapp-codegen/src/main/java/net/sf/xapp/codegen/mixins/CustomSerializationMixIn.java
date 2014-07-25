/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.*;

import java.util.List;

import static net.sf.xapp.codegen.mixins.StringSerializationMixIn.*;

public class CustomSerializationMixIn extends SerializationGenerater implements MixIn<ComplexType> {
    void init(ComplexType complexType, CodeFile ct) {
        //ct.addImplements("Serializable");
        //ct.addImplements("DataSerializable");
        ct.addImport("java.io.*");
        //ct.addImport("ngpoker.common.framework.DataSerializable");
        //ct.addImport("java.io.Serializable");
    }

    public void genRead(ComplexType complexType, CodeFile ct) {
        ct.method2("readData", "void", "IOException", "DataInput in");
        genCollectionHelperVars(complexType, ct, false);
        genOptionalHelperVars(complexType, ct);
        List<Field> fields = complexType.resolveFields(true);
        for (Field field : fields) {
            if (field.isTransient()) {
                continue;
            }
            if (field.isOptional()) {
                ct.line("isNotNull = in.readBoolean()");
                ct.startBlock("if(isNotNull)");
                genReadBlock(ct, field);
                ct.endBlock();
            } else {
                genReadBlock(ct, field);
            }
        }
    }

    public void genWrite(ComplexType complexType, CodeFile ct) {
        List<Field> fields = complexType.resolveFields(true);
        ct.method2("writeData", "void", "IOException", "DataOutput out");
        genOptionalHelperVars(complexType, ct);
        for (Field field : fields) {
            if (field.isTransient()) {
                continue;
            }
            if (field.isOptional()) {
                ct.line("isNotNull = %s != null", field.getName());
                ct.line("out.writeBoolean(isNotNull)");
                ct.startBlock("if(isNotNull)");
                genWriteBlock(ct, field);
                ct.endBlock();
            } else {
                genWriteBlock(ct, field);
            }
        }
    }

    private static void genWriteBlock(CodeFile ct, Field field) {
        String fn = field.getName();
        Type ft = field.getType();
        boolean reference = field.isReference();
        if (field.isList()) {
            ct.line("out.writeInt(%s.size())", fn);
            String varname = itemVarname(fn);
            ct.startBlock("for(%1$s %2$s : %3$s)", ft.getName(), varname, fn);
            genWriteObj(ct, varname, ft, reference);
            ct.endBlock();
        } else if (field.isMap()) {
            Type mapKeyType = field.mapKeyType();
            ct.line("out.writeInt(%s.size())", fn);
            ct.startBlock("for(Map.Entry<%s, %s> _entry : %s.entrySet())", mapKeyType, ft, fn);
            ct.line("%s _key = _entry.getKey()", mapKeyType);
            ct.line("%s _value = _entry.getValue()", ft);
            genWriteObj(ct, "_key", mapKeyType, false);
            genWriteObj(ct, "_value", ft, reference);
            ct.endBlock();

        } else {
            genWriteObj(ct, "" + fn, ft, reference);
        }
    }

    private static void genWriteObj(CodeFile ct, String varname, Type type, boolean reference) {
        String tn = type.getName();
        if (type instanceof ComplexType && reference) {
            ct.line("out.writeUTF(%s.getKey())", varname);
        } else if (type instanceof ComplexType && ((ComplexType) type).isAbstract()) {
            ct.line("out.writeInt(%s.type().getId())", varname, tn);
            ct.line("%s.writeData(out)", varname);
        } else if (type instanceof ComplexType) {
            ct.line("%s.writeData(out)", varname);
        } else if (type instanceof PrimitiveType) {
            ct.line("out.write%2$s(%1$s)", varname, ioNameForPrimitive(tn));
        } else if (tn.equals("ObjectType")) {
            ct.line("out.writeInt(%s.getId())", varname, tn);
        } else if (type instanceof EnumType) {
            ct.line("out.writeInt(%s.ordinal())", varname);
        }
    }

    private static String ioNameForPrimitive(String name) {
        if (name.equals("Integer")) {
            return "Int";
        } else if (name.equals("String")) {
            return "UTF";
        } else {
            return name;
        }
    }

    private static void genOptionalHelperVars(ComplexType complexType, CodeFile ct) {
        if (!complexType.fields(FieldMatcher.OPTIONAL).isEmpty()) {
            ct.line("boolean isNotNull");
        }
    }

    private static void genReadBlock(CodeFile ct, Field field) {
        String fn = field.getName();
        Type ft = field.getType();
        boolean reference = field.isReference();
        if (field.isList()) {
            ct.line("length = in.readInt()");
            ct.line("%s = new %s<%s>(length)", fn, field.isSet() ? "LinkedHashSet" : "ArrayList", ft.getName());
            ct.startBlock("for(int i=0; i<length; i++)");
            String varname = itemVarname(fn);
            genReadObj(ct, varname, ft, reference);
            ct.line("%1$s.add(%2$s)", fn, varname);
            ct.endBlock();
        } else if (field.isMap()) {
            Type mapKeyType = field.mapKeyType();
            ct.line("length = in.readInt()");
            ct.line("%s = new LinkedHashMap<%s, %s>()", fn, mapKeyType, ft);
            ct.startBlock("for(int i=0; i<length; i++)");
            String mapKeyVarName = mapKeyVarname(fn);
            String mapValueVarName = mapValueVarname(fn);
            genReadObj(ct, mapKeyVarName, mapKeyType, false);
            genReadObj(ct, mapValueVarName, ft, reference);
            ct.line("%s.put(%s, %s)", fn, mapKeyVarName, mapValueVarName);
            ct.endBlock();
        } else {
            genReadObj(ct, fn, ft, reference);
        }
    }

    private static void genReadObj(CodeFile ct, String varname, Type type, boolean reference) {
        String tn = type.getName();
        if (type instanceof ComplexType && reference) {
            ct.line("%s = new Ref<%s>(%s.class, in.readUTF())", varname, tn, tn);
        } else if (type instanceof ComplexType && ((ComplexType) type).isAbstract()) {
            ct.line("%s = (%s) ng.Global.create(in.readInt())", varname, tn);
            ct.line("%s.readData(in)", varname);
        } else if (type instanceof ComplexType) {
            ct.line("%s = new %s()", varname, tn);
            ct.line("%s.readData(in)", varname);
        } else if (type instanceof PrimitiveType) {
            ct.line("%s = in.read%s()", varname, ioNameForPrimitive(tn));
        } else if (tn.equals("ObjectType")) {
            ct.line("%s = ng.Global.getObjectType(in.readInt())", varname);
        } else if (type instanceof EnumType) {
            ct.line("%1$s = %2$s.values()[in.readInt()]", varname, tn);
        }
    }

}