/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.LobbyType;
import net.sf.xapp.codegen.model.Model;

public class LobbyPropertyEnumGenerator
{
    private final GeneratorContext generatorContext;

    public LobbyPropertyEnumGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public CodeFile generate(Model model)
    {
        //generate an enum for all error codes
        CodeFile cf = generatorContext.createJavaFile(model.getBaseModule());
        new GenericMixIn(model.getCorePackageName()).mixIn("LobbyPropertyEnum", cf);
        for (LobbyType lobbyType: model.lobbyTypes())
        {
            for (Field field : lobbyType.resolveFields(true))
            {
                cf.addSimpleEnumValue(field.getName());
            }
        }
        return cf;
    }
}
