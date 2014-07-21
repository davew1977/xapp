/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;
import net.sf.xapp.utils.ReflectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenericMixIn implements MixIn
{
    private String packageName;

    public GenericMixIn(String aPackage)
    {
        packageName = aPackage;
    }

    public void mixIn(Object obj, CodeFile ct)
    {
        ct.setPackage(packageName);
        String name = (String) (obj instanceof String ? obj : ReflectionUtils.call(obj, "getName"));
        String comment = (String) (obj instanceof String ? "" : ReflectionUtils.call(obj, "getDescription"));
        ct.setName(name);
        ct.docLine("Generated %s", SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
        if (comment!=null)
        {
            String[] lines = comment.split("\n");
            for (String line : lines)
            {
                ct.docLine(line);
            }
        }
    }
}