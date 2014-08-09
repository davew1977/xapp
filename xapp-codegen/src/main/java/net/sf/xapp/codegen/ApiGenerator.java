/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;
import net.sf.xapp.codegen.mixins.GenHelper;
import net.sf.xapp.codegen.mixins.GenericMixIn;
import net.sf.xapp.codegen.mixins.MessageInterfaceMixIn;
import net.sf.xapp.codegen.model.Api;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.Message;
import net.sf.xapp.codegen.model.TransientApi;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class ApiGenerator
{
    private final GeneratorContext generatorContext;

    public ApiGenerator(GeneratorContext generatorContext)
    {
        this.generatorContext = generatorContext;
    }

    public List<CodeFile> genApiClasses(List<TransientApi> apis)
    {
        ArrayList<CodeFile> files = new ArrayList<CodeFile>();
        for (TransientApi api : apis)
        {
            files.addAll(genApiClasses(api));
        }
        return files;
    }

    public List<CodeFile> genApiClasses(TransientApi api)
    {
        ArrayList<CodeFile> files = new ArrayList<CodeFile>();
        files.add(genInInterface(api));
        files.add(genAdaptor(api));
        return files;
    }

    public CodeFile genInInterface(Api api)
    {
        CodeFile cf = generatorContext.createJavaFile(api);
        cf.setInterface();
        cf.addImport("net.sf.xapp.net.common.framework.*");
        cf.addImport("net.sf.xapp.net.common.types.*");
        new GenericMixIn(api.getPackageName()).mixIn(api.getName(), cf);
        new MessageInterfaceMixIn().mixIn(api, cf);
        return cf;
    }

    CodeFile genAdaptor(TransientApi api)
    {
        CodeFile cf = generatorContext.createJavaFile(api);
        //cf.setAbstract();
        String classSuffix = "Adaptor";
        new GenericMixIn(api.getPackageName()).mixIn(api.getName() + classSuffix, cf);
        String reqHandlerType = format("MessageHandler<%s>", api.getName());
        String adaptorType = format("Adaptor<%s>", api.getName());
        new MessageInterfaceMixIn().mixIn(api, cf);
        cf.addImport(Generator.FWK_PACKAGE_NAME() + ".*");
        cf.addImport("net.sf.xapp.net.common.types.*");
        if (!api.isEmpty())
        {
            cf.addImport(api.messagePackageName() + ".*");
        }
        cf.addImplements(api.getName());
        cf.addImplements(reqHandlerType);
        cf.addImplements(adaptorType);
        cf._final()._protected().field(reqHandlerType, "delegate", Access.READ_ONLY);
        String ekType = null;
        if (api.isEntity() && api.isHideEntityKey())
        {
            ekType = api.getEntityKeyType().toString();
            cf._final()._protected().field(ekType, "key", Access.PRIVATE);

            cf.constructor();
            cf.line("this((String) null)");

            cf.constructor(format("%s key", ekType));
            cf.line("this(key, new Null%s())", reqHandlerType);

            cf.constructor(format("%s key, %s delegate", ekType, reqHandlerType));
            cf.line("this.delegate = delegate");
            cf.line("this.key = key");

            cf.constructor(format("%s delegate", reqHandlerType));
            cf.line("this(null, delegate)");
        }
        else
        {
            cf.constructor();
            cf.line("this(new Null%s())", reqHandlerType);
            cf.constructor(format("%s delegate", reqHandlerType));
            cf.line("this.delegate = delegate");
        }

        if(api.isHidePrincipalField()) {
            Field field = api.principalField();
            String ft = field.getType().getName();
            String fn = field.getName();
            cf._protected().field(ft, fn, Access.PRIVATE);
            if(api.isEntity() && api.isHideEntityKey()) {
                cf.constructor(format("%s %s, %s key, %s delegate", ft, fn, ekType, reqHandlerType));
                cf.line("this.%s = %s", fn, fn);
                cf.line("this.delegate = delegate");
                cf.line("this.key = key");
            } else {
                cf.constructor(format("%s %s, %s delegate", ft, fn, reqHandlerType));
                cf.line("this.%s = %s", fn, fn);
                cf.line("this.delegate = delegate");
            }
        }

        for (Message message : api.getMessages())
        {
            String methodName = GenHelper.methodName(message);
            Method method = cf.getMethod(methodName, GenHelper.typedParamListStr(message, true));
            method.line("%1$s r = new %1$s(%2$s)", message.getName(), GenHelper.paramListStr(message, false));
            method.line("%shandleMessage(r)", message.hasReturnValue() ? "return " : "");
        }
        cf.method("handleMessage", "<T> T", format("InMessage<%s, T> inMessage", api.getName()));
        cf.line("return delegate.handleMessage(inMessage)");
        return cf;
    }
}
