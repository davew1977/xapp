/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

public enum Access
{
    READ_WRITE,READ_ONLY;

    public Access next()
    {
        int i = ordinal();
        return values()[++i % values().length];
    }
}