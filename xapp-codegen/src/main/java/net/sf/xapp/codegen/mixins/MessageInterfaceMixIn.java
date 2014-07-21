/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.codegen.model.Api;
import net.sf.xapp.codegen.model.Message;

import java.util.Set;

import static net.sf.xapp.codegen.mixins.GenHelper.methodName;
import static net.sf.xapp.codegen.mixins.GenHelper.typedParamListStr;

public class MessageInterfaceMixIn implements MixIn<Api>
{
    public void mixIn(Api api, CodeFile ct)
    {
        ct.addImport("java.util.*");
        if(api.isSynchronous())
        {
            ct.addImport(api.getPackageName() + ".to.*");
        }
        for (Message message : api.getMessages())
        {
            String returnType = message.returnType();
            String methodName = methodName(message);
            String params = typedParamListStr(message, true);
            String throwsClause = api.isSynchronous() && !api.getErrors().isEmpty() ?
                                            "GenericException" : null;
            ct.method2(methodName, returnType, throwsClause, params);
            String description = message.getDescription();
            if (description!=null)
            {
                String[] docLines = description.split("\n");
                for (String docLine : docLines)
                {
                    ct.docLine(docLine);
                }
            }
        }
        Set<String> extraImports = api.resolvePackages();
        for (String extraImport : extraImports)
        {
            if (extraImport!=null && !extraImport.equals("java.lang"))
            {
                ct.addImport(String.format("%s.*", extraImport));
            }
        }
    }
}