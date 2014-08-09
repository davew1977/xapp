/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.EnumContext;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Model;

import java.util.Set;

public class TypeEnumGenerator
{
    private static final String TYPE_ENUM_SUFFIX = "TypeEnum";

    private final GeneratorContext generatorContext;

    public TypeEnumGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public CodeFile genTypeEnum(Model model, ComplexType superType)
    {
        Set<ComplexType> subTypes = model.findSubTypes(superType);
        CodeFile cf = generatorContext.createJavaFile(superType);
        String typeName = superType.getName() + TYPE_ENUM_SUFFIX;
        new GenericMixIn(superType.getPackageName()).mixIn(typeName, cf);
        for (ComplexType subType : subTypes)
        {
            cf.addImport(subType.getPackageName() + ".*");
            String subTypeName = subType.getName();
            EnumContext context = cf.newEnumValue(subTypeName);
            context.method("create", subTypeName);
            context.line("return new %s()", subTypeName);
        }
        cf._abstract().method("create", superType.getName());
        cf.method("getId", "int");
        cf.line("return ordinal()");
        cf._static().method("ids", typeName + "[]");
        cf.line("return values()");

        return cf;
    }
}
