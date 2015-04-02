/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package xapp.mdda.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnumType extends AbstractType
{
    private String m_defaultValue;

    private List<String> m_values = new ArrayList<String>();

    public EnumType(String name)
    {
        super(name);
    }

    public EnumType()
    {
    }

    public String getDefaultValue()
    {
        return m_defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        m_defaultValue = defaultValue;
    }

    public List<String> getValues()
    {
        return m_values;
    }

    public void add(String value)
    {
        m_values.add(value);
    }
    
    public void addAll(Collection<String> values)
    {
        m_values.addAll(values);
    }

    public void setValues(List<String> values)
    {
        m_values = values;
    }

    public List<String> validate()
    {
        List<String> errors = super.validate();
        if(m_defaultValue!=null && !m_values.contains(m_defaultValue))
        {
            errors.add(String.format("EnumType %s's default value %s is not a enum value", getName(), getDefaultValue()));
        }
        return errors;
    }

    public boolean containsValue(String item)
    {
        return getValues().contains(item);
    }
}