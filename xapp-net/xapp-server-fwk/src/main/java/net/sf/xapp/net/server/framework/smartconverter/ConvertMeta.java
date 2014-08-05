/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import ngpoker.codegen.model.ComplexType;
import ngpoker.common.framework.LispObj;

import java.util.ArrayList;
import java.util.List;

public class ConvertMeta
{
    private final ComplexType complexType;
    private List<String> errors;

    public ConvertMeta(ComplexType complexType)
    {
        this.complexType = complexType;
        this.errors = new ArrayList<String>();
    }

    public void addError(String error)
    {
        errors.add(error);
    }

    public boolean needsConversion()
    {
        return !errors.isEmpty();
    }

    public List<String> getErrors()
    {
        return errors;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(complexType.getName()).append("{\n");
        for (String error : errors)
        {
            sb.append("\t").append(error).append("\n");
        }
        sb.append("}");
        return sb.toString(); 
    }
}
