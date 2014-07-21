/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;
import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.utils.StringUtils;

import java.util.Collection;

public class WrapperMixIn implements MixIn<Object>
{

    public void mixIn(Object modelObj, CodeFile ct)
    {
        Collection<Method> methods = ct.getMethods();
        ct.clearFields();
        ct.clearConstructors();
        String name = ReflectionUtils.call(modelObj, "getName");
        String varname = StringUtils.decapitaliseFirst(name);
        ct.constructor(name + " " + varname);
        ct.line("%s = %s", varname, varname);
        for (Method method : methods)
        {
            String prefix = method.isVoid() ? "" : "return ";
            method.overwriteLine(0, "%s%s.%s", prefix, varname, method.generateInvocation());
        }
        ct.field(name, varname, Access.READ_ONLY);
    }
}