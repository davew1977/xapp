/*
 *
 * Date: 2010-jun-24
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.codegen.model.FieldSet;

public class ObserverMethodMeta
{
    private final FieldSet fieldSet;
    private final MethodType methodType;
    private final boolean listener;

    public ObserverMethodMeta(FieldSet fieldSet, MethodType methodType, boolean listener)
    {
        this.fieldSet = fieldSet;
        this.methodType = methodType;
        this.listener = listener;
    }

    public FieldSet getFieldSet()
    {
        return fieldSet;
    }

    public MethodType getMethodType()
    {
        return methodType;
    }

    public boolean isListener()
    {
        return listener;
    }
}
