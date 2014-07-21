/*
 *
 * Date: 2010-jun-24
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.Access;
import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;
import net.sf.xapp.codegen.model.Entity;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.FieldSet;

import java.util.Collection;

import static java.lang.String.format;
import static net.sf.xapp.utils.StringUtils.*;
import static net.sf.xapp.codegen.mixins.MethodType.*;

public class ObservableMixin implements MixIn<Entity>
{
    @Override
    public void mixIn(Entity ct, CodeFile cf)
    {
        cf.addImport("static ngpoker.common.util.GeneralUtils.*");
        cf.field(ct.getName() + "Listener", "listener", Access.PRIVATE,
                format("new %1$sListenerAdaptor(null, new Multicaster<%1$sListener>())", ct.getName()));
        cf.method("addListener","void",ct.getName() + "Listener li");
        cf.line("((Multicaster)((Adaptor)listener).getDelegate()).addDelegate(li)");
        cf.method("removeListener","void",ct.getName() + "Listener li");
        cf.line("((Multicaster)((Adaptor)listener).getDelegate()).removeDelegate(li)");
        cf.method("clearListeners","void");
        cf.line("((Multicaster)((Adaptor)listener).getDelegate()).removeAllDelegates()");
        Collection<Method> methods = cf.getMethods();
        for (Method method : methods)
        {
            ObserverMethodMeta methodMeta = method.attachment();
            if (methodMeta != null)
            {
                MethodType type = methodMeta.getMethodType();
                FieldSet fieldSet = methodMeta.getFieldSet();
                if (!methodMeta.isListener())
                {
                    Field field = fieldSet.last();
                    String lastTn = field.typeName();
                    String lastFn = field.getName();
                    String paramName = decapitaliseFirst(lastTn);
                    if (type == CHANGE)
                    {
                        String access = fieldSet.genAccess();
                        method.line("%s oldValue = %s", lastTn, access);
                        String modifier = fieldSet.genModify();
                        if (fieldSet.size() == 1)
                        {
                            method.line("%s = %s", modifier, lastFn);
                        }
                        else
                        {
                            method.line("%s(%s)", modifier, lastFn);
                        }

                    }
                    else if (type == ADD)
                    {
                        method.line("%s.add(%s)", fieldSet.genAccess(), paramName);
                    }
                    else if (type == PUT) {
                        method.line("%s.put(%s, %s)", fieldSet.genAccess(), field.genIndexVarName(), paramName);
                        if(field.containsReferences()) {
                            method.line("%s.init(lookup)", paramName);
                        } else if (field.isReferencedEntityDeclaration(ct)) { //only add to the lookup if there are actually references to this type
                            method.line("lookup.add(%s.class, %s.getKey(), %s)", field.typeName(), paramName, paramName);
                        }
                    }
                    else if (type==ADD_MULTIPLE)
                    {
                        method.line("%s.addAll(%ss)", fieldSet.genAccess(), paramName);
                    }
                    else if (type == REMOVE)
                    {
                        method.line("%s r = %s.remove(%s%s)",
                                lastTn, fieldSet.genAccess(),
                                field.isMap() ? "" : "(int) ",
                                leaf(method.generateParamStr(false), ","));
                    }
                    else if(type == CLEAR)
                    {
                        String access = fieldSet.genAccess();
                        method.line("%s c = %s", field.isMap() ? "Map" : "Collection" , access);
                        method.startBlock("if(!c.isEmpty())");
                        method.line("c.clear()", access);

                        String delegationParams = method.generateParamStr(false);
                        method.line("listener.%s(%s)", type.methodName(fieldSet, true), delegationParams);
                        method.endBlock();
                    }
                    String delegationParams;
                    if (type == CHANGE)
                    {
                        delegationParams = removeLastToken(method.generateParamStr(false), ",");
                        delegationParams += format(", oldValue, %s", lastFn);
                        delegationParams = stripIfStartsWith(delegationParams, ", ");
                        method.startBlock("if(!objEquals(oldValue, %s))", lastFn);
                        method.line("listener.%s(%s)", type.methodName(fieldSet, true), delegationParams);
                        method.endBlock();
                    }
                    else if(type==REMOVE)
                    {
                        delegationParams = method.generateParamStr(false);
                        method.line("listener.%s(%s, r)", type.methodName(fieldSet, true), delegationParams);
                    }
                    else if(type!=CLEAR)
                    {
                        delegationParams = method.generateParamStr(false);
                        method.line("listener.%s(%s)", type.methodName(fieldSet, true), delegationParams);
                    }
                    if(type == REMOVE)
                    {
                        method.line("return r");
                    }
                }
                else
                {
                    String delegationParams = method.generateParamStr(false);
                    if(type==CHANGE)
                    {
                        delegationParams = removeTokenAt(delegationParams, ",", -2);
                    }
                    else if(type==REMOVE)
                    {
                        delegationParams = removeTokenAt(delegationParams, ",", -1);
                    }
                    method.line("%s(%s)",type.methodName(fieldSet, false), delegationParams);
                }
            }
        }
    }

}