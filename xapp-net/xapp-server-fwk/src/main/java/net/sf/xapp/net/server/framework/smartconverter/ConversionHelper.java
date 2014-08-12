/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import ngpoker.codegen.model.ComplexType;
import ngpoker.codegen.model.EnumType;
import ngpoker.codegen.model.Field;
import ngpoker.codegen.model.PrimitiveType;
import net.sf.xapp.net.common.framework.LispObj;

import java.util.List;

import static java.lang.String.format;

public class ConversionHelper
{

    public static ConvertMeta analyze(ComplexType type, LispObj lispObj)
    {
        ConvertMeta convertMeta = new ConvertMeta(type);
        List<Field> fields = type.resolveFieldsBasic(true);
        int requiredCount = fields.size();
        int actualCount = lispObj.size();
        if(actualCount!=requiredCount)
        {
            convertMeta.addError(format("field count does not match: required: %s, actual %s",
                    requiredCount, actualCount));
        }
        for (int i = 0; i < Math.max(requiredCount, actualCount); i++)
        {
            if(i<actualCount && i<requiredCount)
            {
                Object item = lispObj.itemAt(i);
                Field field = fields.get(i);
                if(!lispObj.isListAt(i) &&
                        field.isMandatory() &&
                        field.isCollectionOrComplex())
                {
                    convertMeta.addError(format("field at %s, %s, found: %s",
                            i, field, item));
                }
                if(field.isMandatory() && isNullOrEmpty(item))
                {
                    convertMeta.addError(format("mandatory field \"%s\" is null", field));
                }
                if(!field.isCollectionOrComplex() && item instanceof List)
                {
                    convertMeta.addError(format("primitive field \"%s\" has complex value", field));
                }
                else if(!field.isCollectionOrComplex() && !isNullOrEmpty(item))
                {
                    String strValue = (String) item;
                    if(field.getType() instanceof EnumType && !((EnumType) field.getType()).isSkipGeneration())
                    {
                        EnumType enumType = (EnumType) field.getType();
                        if(!enumType.containsValue(strValue))
                        {
                            convertMeta.addError(format("enum value \"%s\" not found in \"%s\", field: \"%s\"",
                                    strValue, enumType, field));
                        }
                    }
                    else if(field.getType() instanceof PrimitiveType)
                    {
                        PrimitiveType primitiveType = (PrimitiveType) field.getType();
                        if(primitiveType.getName().equals("Boolean") &&
                                !strValue.matches("true|false"))
                        {
                            convertMeta.addError(format("boolean value \"%s\" not valid, field: \"%s\"",
                                    strValue, field));
                        }
                    }
                }
            }
            else if(i>=requiredCount)
            {
                convertMeta.addError(format("field removed (?) : %s", lispObj.itemAt(i)));
            }
            else
            {
                Field field = fields.get(i);
                convertMeta.addError(format("field added (?) : %s", field));
            }
        }
        return convertMeta;
    }

    public static boolean isNullOrEmpty(Object s)
    {
        return s == null || s.equals("");
    }
}
