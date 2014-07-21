/*
 *
 * Date: 2010-jun-24
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.application.utils.codegen.Method;
import net.sf.xapp.codegen.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static net.sf.xapp.utils.StringUtils.*;

public class ObserverAPIMixin implements MixIn<Entity>
{
    private boolean isListener;

    public ObserverAPIMixin(boolean listener)
    {
        isListener = listener;
    }

    @Override
    public void mixIn(Entity ct, CodeFile cf)
    {
        List<FieldSet> lines = ct.deepAnalyze();
        for (FieldSet line : lines)
        {
            Field field = line.last();
            String ip = line.genIndexParams(true);
            String tn = field.getType().getName();
            if(field.isList())
            {
                cf.attach(new ObserverMethodMeta(line, MethodType.ADD, isListener));
                cf.method(MethodType.ADD.methodName(line, isListener), "void", removeLastToken(ip, ","),
                        format("%s %s", tn, decapitaliseFirst(tn)));
                cf.attach(new ObserverMethodMeta(line, MethodType.ADD_MULTIPLE, isListener));
                cf.method(MethodType.ADD_MULTIPLE.methodName(line, isListener), "void", removeLastToken(ip, ","),
                        format("%s %s", GenHelper.listTypeDecl(field.getType()), decapitaliseFirst(tn) + "s"));
                if (!field.isSet()) //todo allow remove for sets
                {
                    cf.attach(new ObserverMethodMeta(line, MethodType.REMOVE, isListener));
                    String removedSuffix = isListener ? ", " + tn + " removed" : "";
                    cf.method(MethodType.REMOVE.methodName(line, isListener), isListener ? "void" : tn, ip + removedSuffix);
                }
                cf.attach(new ObserverMethodMeta(line, MethodType.CLEAR, isListener));
                cf.method(MethodType.CLEAR.methodName(line, isListener), "void", removeLastToken(ip, ","));
            }
            else if(field.isMap()) {
                cf.attach(new ObserverMethodMeta(line, MethodType.PUT, isListener));
                cf.method(MethodType.PUT.methodName(line, isListener), "void", removeLastToken(ip, ","),
                        format("%s %s", field.mapKeyType(), field.genIndexVarName()),
                        format("%s %s", tn, decapitaliseFirst(tn)));
                cf.attach(new ObserverMethodMeta(line, MethodType.REMOVE, isListener));
                String removedSuffix = isListener ? ", " + tn + " removed" : "";
                cf.method(MethodType.REMOVE.methodName(line, isListener), isListener ? "void" : tn, ip + removedSuffix);
                cf.attach(new ObserverMethodMeta(line, MethodType.CLEAR, isListener));
                cf.method(MethodType.CLEAR.methodName(line, isListener), "void", removeLastToken(ip, ","));
            }
            else
            {

                String params = isListener ? format("%1$s oldValue, %1$s newValue", tn) :
                            format("%s %s", tn, field.getName());
                cf.attach(new ObserverMethodMeta(line, MethodType.CHANGE, isListener));
                cf.method(MethodType.CHANGE.methodName(line, isListener), "void", ip, params);
            }
            cf.attach(null);
        }
    }

    /**
     * reverse engineer a source file
     * @param apiType
     * @param cf
     * @return
     */
    public static TransientApi createApi(CodeFile cf, Map<String, Type> typeLookup, TransientApiType apiType)
    {
        TransientApi api = new TransientApi(apiType);
        api.setName(cf.getName());
        Collection<Method> methods = cf.getMethods();
        for (Method method : methods)
        {
            List<Field> fields = new ArrayList<Field>();
            List<String> args = method.getParams();
            for (String arg : args)
            {
                String[] s = arg.split(" ");
                String typeName = s[0].trim();
                String fieldName = s[1].trim();
                Field f = new Field(fieldName, typeLookup.get(typeName));
                f.setList(typeName.startsWith("List<"));
                f.setOptional(true);
                fields.add(f);
            }
            Message m = Message.create(capitalizeFirst(method.getName()), fields);
            m.setReturnType(typeLookup.get(method.getReturnType()));
            api.add(m);
        }
        return api;
    }
}
