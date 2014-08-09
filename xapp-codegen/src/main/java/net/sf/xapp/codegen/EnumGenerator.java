/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.model.EnumType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EnumGenerator
{
    private final GeneratorContext generatorContext;

    public EnumGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public CodeFile genEnum(EnumType enumType)
    {
        CodeFile ct = generatorContext.createJavaFile(enumType);
        new GenericMixIn(enumType.getPackageName()).mixIn(enumType, ct);
        Set<String> stringList = new LinkedHashSet<String>(enumType.getValues());
        List<String> values = new ArrayList<String>();
        for (String s : stringList)
        {
            values.add(s.trim());
        }
        ct.setEnum(values);
        return ct;
    }
}
