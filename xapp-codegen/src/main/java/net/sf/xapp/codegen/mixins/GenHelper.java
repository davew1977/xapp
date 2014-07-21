/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.model.*;

import java.util.List;

public class GenHelper
{
    public static String methodName(Message m)
    {
        return StringUtils.decapitaliseFirst(m.getName());
    }

    public static String typedParamListStr(Message m, boolean filterKeyFields)
    {
        return paramListStr(m, filterKeyFields, true);
    }

    public static String paramListStr(Message m, boolean filterKeyFields)
    {
        return paramListStr(m, filterKeyFields, false);
    }

    public static String declaredTypeName(Field field)
    {
        String typeName = field.getType().getName();
        return field.isMap() ? mapTypeDecl(field) :
                field.isSet() ? "Set<" + field.getType() + ">" :
                field.isList() ? listTypeDecl(field.getType()) : typeName;
    }

    //todo find usage of this and make sure maps are covered
    public static String listTypeDecl(Type type)
    {
        return "List<" + type + ">";
    }

    public static String mapTypeDecl(Field field) {
        return String.format("Map<%s, %s>", field.mapKeyType(), field.getType());
    }

    public static String paramListStr(List<Field> fields)
    {
        return paramListStr(fields, true);
    }

    public static String paramListStr(Message m, boolean filterKeyFields, final boolean includeTypes) {

        List<Field> fields = m.resolveFields(true);
        if (filterKeyFields) {
            final boolean hideEntityKeyField = m.api.isEntity() && m.api.isHideEntityKey();
            final boolean hidePrincipalField = m.api.getPrincipalType() != null && m.api.isHidePrincipalField();
            fields = CollectionsUtils.filter(fields, new Filter<Field>() {
                @Override
                public boolean matches(Field field) {
                    boolean excludeEntityKeyField = hideEntityKeyField && field instanceof EntityKeyField;
                    boolean excludePrincipalField = hidePrincipalField && field instanceof PrincipalField;
                    return !(excludeEntityKeyField || excludePrincipalField);
                }
            });
        }
        return paramListStr(fields, includeTypes);
    }
    public static String paramListStr(List<Field> fields, final boolean includeTypes)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++)
        {
            Field p = fields.get(i);
            if (includeTypes)
            {
                sb.append(declaredTypeName(p)).append(' ');
            }
            sb.append(p.getName());
            if (i < fields.size() - 1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}