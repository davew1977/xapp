/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.model.Api;
import net.sf.xapp.codegen.model.EnumType;
import net.sf.xapp.codegen.model.Model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModelEnumGenerator
{
    private final EnumGenerator enumGenerator;

    public ModelEnumGenerator(EnumGenerator enumGenerator)
    {
        this.enumGenerator = enumGenerator;
    }

    public CodeFile generateAllErrorEnum(Model model, List<? extends Api> apis)
    {
        //generate an enum for all error codes
        EnumType allErrorsEnum = new EnumType("ErrorCode");
        Set<String> errors = new LinkedHashSet<String>();
        for (Api api : apis)
        {
            errors.addAll(api.getErrors());
        }
        allErrorsEnum.addAll(errors);
        allErrorsEnum.add("ENTITY_ALREADY_EXISTS");
        allErrorsEnum.setPackageName(model.getCorePackageName());
        allErrorsEnum.setModule(model.getBaseModule());
        CodeFile codeFile = enumGenerator.genEnum(allErrorsEnum);
        return codeFile;
    }

    public CodeFile generateAllApiEnum(Model model, List<? extends Api> apis)
    {
        //generate an enum for all error codes
        EnumType allApisEnum = new EnumType("ApiType");
        for (Api api : apis)
        {
            allApisEnum.add(StringUtils.camelToUpper(api.getName()));
        }
        allApisEnum.setPackageName(model.getCorePackageName());
        allApisEnum.setModule(model.getBaseModule());
        return enumGenerator.genEnum(allApisEnum);
    }
}
