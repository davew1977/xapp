/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import net.sf.xapp.Global;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Field;
import net.sf.xapp.codegen.model.Message;
import net.sf.xapp.codegen.model.Type;
import net.sf.xapp.net.common.framework.LispObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SmartConverter
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private boolean debugEnabled;
    private Map<String, ComplexType> allTypesByClassname;
    private Map<String, ComplexType> allSubclassesByName;

    public SmartConverter(boolean debugEnabled)
    {
        allTypesByClassname = Global.deriveAllTypes();
        allSubclassesByName = new HashMap<String, ComplexType>();
        for (ComplexType complexType : allTypesByClassname.values())
        {
            String subClassKey = subClassKey(complexType);
            if(subClassKey!=null)
            {
                allSubclassesByName.put(subClassKey, complexType);
            }
        }
        this.debugEnabled = debugEnabled;
    }

    private String subClassKey(ComplexType complexType)
    {
        if(complexType instanceof Message)
        {
            Message message = (Message) complexType;
            return message.api.getName() + "_" + complexType.getName();
        }
        else if(complexType.getSuperType()!=null)
        {
            return complexType.getName();
        }
        return null;
    }

    public ConvertResult<LispObj> convert(String lispObj, String rootType, Converter helper)
    {
        ComplexType type = allTypesByClassname.get(rootType);
        return convert_internal(new LispObj(lispObj), type, helper);
    }

    private ConvertResult<LispObj> convert_internal(LispObj rootLispObj, ComplexType rootComplexType, Converter helper)
    {
        LispObj originalRootLispObj = rootLispObj;
        if (rootComplexType.isAbstract())
        {
            rootComplexType = allSubclassesByName.get(rootLispObj.get(0));
            rootLispObj = rootLispObj.subTree(1);
        }

        if (debugEnabled)
        {
            log.debug(ConversionHelper.analyze(rootComplexType, rootLispObj).toString());
        }
        ConvertResult<LispObj> result = helper.convert(rootLispObj, rootComplexType);
        boolean converted = result.isConverted();
        rootLispObj = result.getTarget();
        ConvertMeta convertMeta = ConversionHelper.analyze(rootComplexType, rootLispObj);
        assert !convertMeta.needsConversion() : convertMeta + " original: " + rootLispObj;
        for (int i = 0; i < rootComplexType.getFields().size(); i++)
        {
            Field field = rootComplexType.getFields().get(i);
            Type fieldType = field.getType();
            if (field.isList() && fieldType instanceof ComplexType)
            {
                for (int j = 0; j < rootLispObj.getList(i).size(); j++)
                {
                    LispObj item = rootLispObj.subTree(i, j);
                    result = convert_internal(item, (ComplexType) fieldType, helper);
                    converted |= result.isConverted();
                    rootLispObj.set(result.getTarget(), i, j);
                }
            }
            else if (field.isMap() && fieldType instanceof ComplexType)
            {
                for (int j = 0; j < rootLispObj.getList(i).size(); j++)
                {              //todo test!! and convert map keys as well
                    LispObj item = rootLispObj.subTree(i, j, 1);
                    result = convert_internal(item, (ComplexType) fieldType, helper);
                    converted |= result.isConverted();
                    rootLispObj.set(result.getTarget(), i, j,1);
                }
            }
            else if (fieldType instanceof ComplexType)
            {
                if (rootLispObj.isListAt(i))
                {
                    LispObj item = rootLispObj.subTree(i);
                    result = convert_internal(item, (ComplexType) fieldType, helper);
                    converted |= result.isConverted();
                    rootLispObj.set(result.getTarget(), i);
                }
            }
            else
            {
                //nothing to do for primitives
            }
        }

        return new ConvertResult<LispObj>(converted, originalRootLispObj);
    }


}
