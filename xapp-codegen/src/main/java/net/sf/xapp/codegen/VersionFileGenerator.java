/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.Model;

public class VersionFileGenerator
{
    private final GeneratorContext generatorContext;

    public VersionFileGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public CodeFile genVersionFile(Model model)
    {
        CodeFile cf = generatorContext.createJavaFile(model.getBaseModule());
        cf.setInterface();
        new GenericMixIn("xapp").mixIn("Version", cf);
        cf._static()._final().field("String", "VERSION", Access.PUBLIC, String.format("\"%s\"", model.getVersion()));
        return cf;
    }
}
