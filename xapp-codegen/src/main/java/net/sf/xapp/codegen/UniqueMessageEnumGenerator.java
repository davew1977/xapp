/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.EnumContext;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.*;

import java.util.List;
import java.util.Map;

public class UniqueMessageEnumGenerator
{
    private final GeneratorContext generatorContext;

    public UniqueMessageEnumGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public CodeFile generate(Model model, List<TransientApi> apis)
    {
        List<Message> messages = model.deriveAllMessages();

        ObjectIdHelper helper = new ObjectIdHelper(model.updateMessageIds());
        return generatorContext.isLight() ?
                generateClientVersion(model, messages, helper) :
                generateServerVersion(model, messages, helper, "MessageTypeEnum");
    }

    public CodeFile generateObjectTypeEnum(Model model) {
        return generateServerVersion(model, model.concreteTypes(), new ObjectIdHelper(model.updateObjectIds()), "ObjectTypeEnum");
    }

    private CodeFile generateServerVersion(Model model, List<? extends ComplexType> complexTypes, ObjectIdHelper helper, String targetClassName)
    {
        //generate an enum for all error codes
        CodeFile cf = generatorContext.createJavaFile(model);
        new GenericMixIn(model.getCorePackageName()).mixIn(targetClassName, cf);
        cf.addImport("net.sf.xapp.net.common.framework.Message");
        cf.addImport("net.sf.xapp.net.common.framework.ObjectType");
        cf.addImplements("ObjectType");
        for (ComplexType message : complexTypes) {
            String enumValue = String.format("%s(%s)", message.uniqueObjectKey(), helper.getId(message));
            EnumContext context = cf.newEnumValue(enumValue);
            String fullClassName = message.className();
            context.method("create", fullClassName);
            context.line("return new %s()", fullClassName);
        }

        cf._final().field("int", "id");
        cf._private().constructor("int id");
        cf.line("this.id = id");
        cf.method("getId", "int");
        cf.line("return id");
        addValuesArray(complexTypes, cf, helper);
        return cf;
    }

    private CodeFile generateClientVersion(Model model, List<Message> messages, ObjectIdHelper helper)
    {
        //generate an enum for all error codes
        CodeFile cf = generatorContext.createJavaFile(model);
        new GenericMixIn(model.getCorePackageName()).mixIn("MessageTypeEnum", cf);
        for (Message message : messages)
        {
            String enumValue = String.format("%s(%s)", message.uniqueObjectKey(), helper.getId(message));
            cf.addSimpleEnumValue(enumValue);
        }
        cf.addImport("net.sf.xapp.net.common.framework.Message");
        cf.startBlock("switch(this)");
        for (Message message : messages)
        {
            String fullClassName = message.api.messagePackageName() + "." + message.getName();
            if (message.api.isClientVisible())
            {
                cf.line("case %s: return new %s()",message.uniqueObjectKey(), fullClassName);
            }
            else
            {
                cf.line("case %s: throw new RuntimeException(\"%s not available client side\")",
                        message.uniqueObjectKey(), fullClassName);
            }
        }

        cf.line("default: throw new RuntimeException()");
        cf.endBlock();

        addValuesArray(messages, cf, helper);
        return cf;
    }

    private void addValuesArray(List<? extends ComplexType> messages, CodeFile cf, ObjectIdHelper helper) {
        cf.addImport("java.util.Map");
        cf.addImport("java.util.HashMap");
        String mapType = "Map<Integer, ObjectType>";
        cf._static()._final().field(mapType, "MAP", Access.PUBLIC, "createTypeMap()");
        cf._static()._final().method("createTypeMap", mapType);
        cf.line("%s map = new Hash%s()", mapType, mapType);
        for (ComplexType message : messages) {
            cf.line("map.put(%s, %s)", helper.getId(message), message.uniqueObjectKey());
        }
        cf.line("return map");
    }

    public interface IdHelper {
        int getId(ComplexType o);
    }

    private static class ObjectIdHelper implements IdHelper {
        private Map<String, ObjectId> idMap;

        public ObjectIdHelper(Map<String, ObjectId> idMap) {
            this.idMap = idMap;
        }

        @Override
        public int getId(ComplexType o) {
            return idMap.get(o.uniqueObjectKey()).getId();
        }
    }
}
