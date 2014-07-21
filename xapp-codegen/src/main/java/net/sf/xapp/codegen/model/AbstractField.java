/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.Transient;

public abstract class AbstractField implements Cloneable
{
    private String m_name;
    private Type m_type;
    private boolean m_optional;
    private Access m_access = Access.READ_ONLY;

    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    @Reference
    public Type getType()
    {
        return m_type;
    }

    public void setType(Type type)
    {
        m_type = type;
    }

    public abstract boolean isList();

    public String toString()
    {
        return m_type + (isList() ? "[]": "" ) + " " + m_name;
    }

    public boolean isOptional()
    {
        return m_optional;
    }

    public void setOptional(boolean optional)
    {
        m_optional = optional;
    }

    public Access getAccess()
    {
        return m_access;
    }

    public void setAccess(Access access)
    {
        m_access = access;
    }

    @Transient
    public boolean isReadOnly()
    {
        return m_access== Access.READ_ONLY;
    }

    @Transient
    public boolean isWritable()
    {
        return m_access== Access.READ_WRITE;
    }

    @Transient
    public boolean isMandatory()
    {
        return !isOptional();
    }

    public AbstractField clone() throws CloneNotSupportedException
    {
        return (AbstractField) super.clone();
    }
}