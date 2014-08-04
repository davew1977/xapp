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

public class TypeGenerator {
    private final GenContext genContext;
    private final DomainTypeGenerator domainTypeGenerator;
    private final TypeEnumGenerator typeEnumGenerator;
    private final EnumGenerator enumGenerator;

    public TypeGenerator(GenContext genContext, DomainTypeGenerator domainTypeGenerator, TypeEnumGenerator typeEnumGenerator, EnumGenerator enumGenerator) {
        this.genContext = genContext;
        this.domainTypeGenerator = domainTypeGenerator;
        this.typeEnumGenerator = typeEnumGenerator;
        this.enumGenerator = enumGenerator;
    }

    public List<CodeFile> generateTypes(Model model) {
        List<CodeFile> files = new ArrayList<CodeFile>();
        List<ComplexType> complexTypes = model.all(ComplexType.class);
        for (ComplexType complexType : complexTypes) {
            if (complexType.shouldGenerate()) {
                files.add(domainTypeGenerator.genDomainClass(complexType));
            }
            if (model.isXappPluginEnabled()) {
                files.addAll(new XappMixIn().generate(genContext, complexType));
            }
        }
        List<EnumType> enumTypes = model.all(EnumType.class);
        for (EnumType enumType : enumTypes) {
            if (enumType.shouldGenerate()) {
                files.add(enumGenerator.genEnum(enumType));
            }
        }
        return files;
    }
}
