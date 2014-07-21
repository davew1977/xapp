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
import net.sf.xapp.codegen.model.Field;

import static net.sf.xapp.codegen.mixins.StringSerializationMixIn.genParseFromString;

public class StorableMixIn implements MixIn<ComplexType>
{
    public void mixIn(ComplexType vo, CodeFile ct)
    {
        ct.addImplements("Storable");
        ct.addImport("ngpoker.common.framework.Storable");
        if (!vo.isAbstract())
        {
            ct.method("get", "String", "String propName");
            ct.startBlock("switch(LobbyPropertyEnum.valueOf(propName))");
            for (Field field : vo.resolveFields(true))
            {
                if (!field.isCollection())
                {
                    String fn = field.getName();
                    String v = field.getType().getName().equals("String") ?
                            fn :
                            "String.valueOf(" + fn + ")";
                    ct.line("case %s: return %s", fn, v);
                }
            }
            ct.line("default: throw new RuntimeException(\"property \" + propName + \" does not exist in \" + " +
                    "getClass().getSimpleName())");
            ct.endBlock();


            ct.method("set", "void", "String propName, String value");
            ct.startBlock("switch(LobbyPropertyEnum.valueOf(propName))");
            for (Field field : vo.resolveFields(true))
            {
                if (!field.isCollection() && field.isWritable())
                {
                    String fn = field.getName();
                    ct.line("case %s: %s = %s", fn, fn, genParseFromString(field.getType(), "value"));
                    ct.line("break");
                }
            }
            ct.line("default: throw new RuntimeException(\"property \" + propName + \" does not exist or is not mutable in \" + " +
                    "getClass().getSimpleName())");
            ct.endBlock();

            //primitive lists
            ct.method("set", "void", "String propName, int index, String value, ListOp op");
            ct.startBlock("switch(LobbyPropertyEnum.valueOf(propName))");
            for (Field field : vo.resolveFields(true))
            {
                if (field.isCollection() && field.isWritable())
                {
                    String fn = field.getName();
                    ct.startBlock("case %s:", fn);
                    ct.startBlock("switch(op)");
                    ct.line("case UPDATE: %s.set(index, %s)", fn, genParseFromString(field.getType(), "value"));
                    ct.line("break");
                    ct.line("case ADD: %s.add(%s)", fn, genParseFromString(field.getType(), "value"));
                    ct.line("break");
                    ct.line("case REMOVE: %s.remove(index)", fn);
                    ct.line("break");
                    ct.endBlock();
                    ct.endBlock();
                    ct.line("break");
                }
            }
            ct.line("default: throw new RuntimeException(\"property \" + propName + \" does not exist or is not a primitive list \" + " +
                    "getClass().getSimpleName())");
            ct.endBlock();

        }
    }
}