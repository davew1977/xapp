/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;

import java.util.Collection;

public class MethodLoggingMixIn implements MixIn
{
    private boolean logGetters;
    private boolean logSetters;

    public MethodLoggingMixIn(boolean logGetters, boolean logSetters)
    {
        this.logGetters = logGetters;
        this.logSetters = logSetters;
    }

    public void mixIn(Object obj, CodeFile ct)
    {
        Collection<Method> methods = ct.getMethods();
        for (Method method : methods)
        {
            if(!logGetters && method.isGetter())
            {
                continue;
            }
            if(!logSetters && method.isSetter())
            {
                continue;
            }
            if(method.isVoid())
            {
                method.line("System.out.println(\"%s() called on \"+this)", method.getName());
            }
            else
            {
                method.lineAtStart("System.out.println(\"%s() called on \"+this)", method.getName());
            }
        }

    }
}