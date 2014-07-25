/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;
import net.sf.xapp.codegen.mixins.GenHelper;
import net.sf.xapp.codegen.model.Api;
import net.sf.xapp.codegen.model.Message;
import net.sf.xapp.codegen.model.TransientApi;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class MessageGenerator
{
    private final DomainTypeGenerator domainTypeGenerator;

    public MessageGenerator(GenContext genContext)
    {
        domainTypeGenerator = new DomainTypeGenerator(genContext);
    }

    public List<CodeFile> genMessageClasses(TransientApi api)
    {
        ArrayList<CodeFile> files = new ArrayList<CodeFile>();

        for (Message message : api.getMessages())
        {
            files.add(genInMessageClass(api, message));
        }

        for (Message response : api.deriveResponses())
        {
            files.add(genResponseClass(api, response));
        }
        return files;
    }

    public CodeFile genResponseClass(Api api, Message message)
    {
        CodeFile ct = genMessageClass(api, message);
        ct.addImplements("Response");
        return ct;
    }

    public CodeFile genMessageClass(Api api, Message message)
    {
        CodeFile ct = domainTypeGenerator.genDomainClass(message);
        ct.addImport(api.getPackageName() + ".*");
        return ct;
    }

    public CodeFile genInMessageClass(Api api, Message message)
    {
        CodeFile ct = domainTypeGenerator.genDomainClass(message);
        ct.addImport(api.getPackageName() + ".*");

        boolean hasReturnVal = message.hasReturnValue();
        String returnType = message.genericReturnType();
        //may need to import the return type if not a response
        if (message.getReturnType() != null)
        {
            ct.addImport(message.getReturnType().getPackageName() + "." + message.getReturnType());
        }
        ct.setSuper(format("AbstractInMessage<%s, %s>", api.getName(), returnType));
        List<Method> constructors = ct.getConstructors();
        for (Method constructor : constructors)
        {
            if (constructor.getParams().isEmpty())
            {
                constructor.lineAtStart("super(%s.class,\n\t\t\tMessageTypeEnum.%s, %s)",
                        api.getName(),
                        api.getName() + "_" + message.getName(),
                        message.isPersistent());
            }
            else
            {
                constructor.lineAtStart("this()");
            }
        }
        //generate extra constructor if api has an entityType

        if (api.isEntity())
        {
            ct.method("entityKey", "String");
            ct.line("return key");
        }
        if (api.hasPrincipal())
        {
            ct.method("principal", "Object");
            ct.line("return principal");
        }
        //ct.line("return ApiType.%s", StringUtils.camelToUpper(api.toString()));
        //visit method

        String paramList = GenHelper.paramListStr(message, true);

        ct.method("visit", returnType, api.getName() + " in");
        ct.line("%sin.%s(%s)", hasReturnVal ? "return " : "",
                GenHelper.methodName(message), paramList);
        if (!hasReturnVal)
        {
            ct.line("return null");
        }

        return ct;
    }
}
