/*
 *
 * Date: 2010-jun-22
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.mixins.MethodType;

import java.util.ArrayList;
import java.util.List;

import static net.sf.xapp.application.utils.codegen.AbstractCodeFile.getterName;
import static net.sf.xapp.application.utils.codegen.AbstractCodeFile.setterName;
import static net.sf.xapp.utils.StringUtils.capitalizeFirst;
import static net.sf.xapp.utils.StringUtils.depluralise;
import static net.sf.xapp.codegen.mixins.MethodType.*;

public class FieldSet
{
    private List<Field> fields;

    /**
     * Cloning constructor
     * @param fieldSet
     */
    public FieldSet(FieldSet fieldSet)
    {
        fields = new ArrayList<Field>(fieldSet.fields);
    }

    public FieldSet(List<Field> fields)
    {
        this.fields = fields;
    }

    public List<Field> getFields()
    {
        return fields;
    }

    public void add(Field field)
    {
        fields.add(field);
    }

    public Field last()
    {
        return fields.get(fields.size() - 1);
    }

    public Field penultimate() {

        return fields.get(fields.size() - 2);
    }

    public boolean isLast(Field field)
    {
        return last().equals(field);
    }

    public String genVarName(MethodType methodType)
    {
        List<Field> selectedFields = significantFields();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedFields.size(); i++)
        {
            Field field = selectedFields.get(i);
            String fn = field.getName();
            boolean last = i==selectedFields.size()-1;
            fn = field.isCollection() &&
                    (methodType == PUT || methodType== ADD || methodType== REMOVE || !last)? depluralise(fn) : fn;
            fn = i == 0 ? fn : capitalizeFirst(fn);
            sb.append(fn);
        }
        return sb.toString();
    }

    public List<Field> significantFields() {
        int s = fields.size();
        return s >1 ? fields.subList(s-2, s) : fields;
    }

    public String genIndexParams(boolean includeTypes)
    {
        StringBuilder sb = new StringBuilder();
        List<Field> listFields = Field.filter(fields, FieldMatcher.COLLECTION);
        for (int i = 0; i < listFields.size(); i++)
        {
            Field field = listFields.get(i);
            sb.append(includeTypes ? field.genIndexTypeName() + " ": "").append(field.genIndexVarName());
            if(i<listFields.size()-1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String genAccess()
    {
        if(size()==1)
        {
            return "this." + last().getName();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++)
        {
            Field field = fields.get(i);
            String tn = field.getType().getName();
            String fn = field.getName();
            String indexVar = isLast(field) ? "" : field.isCollection() ? ").get(" + field.genIndexVarName() : "";
            String accessor = getterName(tn, fn);
            sb.append(accessor).append("(").append(indexVar).append(")");
            if(i<fields.size()-1)
            {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public String genModify()
    {
        String accessor = genAccess();
        if(fields.size()==1)
        {
            return accessor;
        }
        else
        {
            return StringUtils.removeLastToken(accessor, ".") + "." + setterName(last().getName());
        }
    }

    public int size()
    {
        return fields.size();
    }

    @Override
    public String toString() {
        return fields.toString();
    }
}
