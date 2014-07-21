/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.XappMixIn;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.EnumType;
import net.sf.xapp.codegen.model.Model;

import java.util.ArrayList;
import java.util.List;

public class TypeGenerator
{
    private final GenContext genContext;
    private final DomainTypeGenerator domainTypeGenerator;
    private final TypeEnumGenerator typeEnumGenerator;
    private final EnumGenerator enumGenerator;

    public TypeGenerator(GenContext genContext, DomainTypeGenerator domainTypeGenerator, TypeEnumGenerator typeEnumGenerator, EnumGenerator enumGenerator)
    {
        this.genContext = genContext;
        this.domainTypeGenerator = domainTypeGenerator;
        this.typeEnumGenerator = typeEnumGenerator;
        this.enumGenerator = enumGenerator;
    }

    public List<CodeFile> generateTypes(Model model)
    {
        List<CodeFile> files = new ArrayList<CodeFile>();
        for (net.sf.xapp.codegen.model.Package aPackage : model.allPackages())
        {
            //generate classes
            List<ComplexType> complexTypes = aPackage.complexTypes();
            for (ComplexType complexType : complexTypes)
            {
                if (complexType.shouldGenerate())
                {
                    files.add(domainTypeGenerator.genDomainClass(complexType));
                }
                if(model.isXappPluginEnabled()) {
                    files.addAll(new XappMixIn().generate(genContext, complexType));
                }
            }

            //inheritence helpers
            /*for (ComplexType complexType : complexTypes)
            {
                if (complexType.isAbstract() && complexType.shouldGenerate())
                {
                    files.add(typeEnumGenerator.genTypeEnum(model, complexType));
                }
            }*/

            //enum types
            for (EnumType enumType : aPackage.enumTypes())
            {
                if (enumType.shouldGenerate())
                {
                    files.add(enumGenerator.genEnum(enumType));
                }
            }
        }
        return files;
    }
}
