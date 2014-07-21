/*
 *
 * Date: 2010-jun-24
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.codegen.model.FieldSet;

import static net.sf.xapp.utils.StringUtils.capitalizeFirst;

public enum MethodType
{
    CHANGE("set", "Changed"),CLEAR("clear", "Cleared"),PUT("add", "Added"),ADD("add", "Added"),ADD_MULTIPLE("add", "Added"),REMOVE("remove", "Removed");

    MethodType(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    String methodName(FieldSet f, boolean isListener)
    {
        String vn = f.genVarName(this);
        return isListener ? vn + suffix : prefix + capitalizeFirst(vn);
    }

    String prefix;
    String suffix;

    public boolean isAdd()
    {
        return this==ADD || this==ADD_MULTIPLE;
    }
}
