/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.StringUtils;
import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.codegen.model.Field;

import java.util.List;

public class EqualsAndHashcodeMixIn implements MixIn<ComplexType>
{
    public void mixIn(ComplexType vo, CodeFile ct)
    {
        ct.method("equals", "boolean", "Object o");
        ct.line("if (this == o) return true");
        ct.line("if (o == null || getClass() != o.getClass()) return false");

        String n = vo.getName();
        String varname = StringUtils.decapitaliseFirst(n);
        ct.line("%s %s = (%s)o", n, varname, n);
        List<Field> fields = vo.resolveFields(true);
        for (Field field : fields)
        {
            if(field.isMandatory())
            {
                String fn = field.getName();
                ct.line("if (%1$s != null ? !%1$s.equals(%2$s.%1$s) : %2$s.%1$s != null) return false", fn, varname);
            }
        }
        ct.line("return true");

        ct.method("hashCode", "int");
        int i=0;
        ct.line("int _result = 0");
        for (Field field : fields)
        {
            if(field.isMandatory())
            {
                String fn = field.getName();
                if(i==0)
                {
                    ct.line("_result = %s !=null ? %s.hashCode() : 0", fn, fn);
                }
                else
                {
                    ct.line("_result = 31 * _result + (%s != null ? %s.hashCode() : 0)",fn, fn);
                }
                i++;
            }
        }
        ct.line("return _result");
    }
}