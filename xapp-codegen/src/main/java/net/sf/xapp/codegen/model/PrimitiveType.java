/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

public class PrimitiveType extends AbstractType
{
    private String javaMapping;

    public PrimitiveType(String name)
    {
        super(name);
    }

    public PrimitiveType()
    {
    }

    public String getJavaMapping()
    {
        return javaMapping;
    }

    public void setJavaMapping(String javaMapping)
    {
        this.javaMapping = javaMapping;
    }
}