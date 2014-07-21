/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import java.util.ArrayList;
import java.util.List;

//TODO create immutable type to extend valud object, messages should also extend this and have the (commented out) validation below
public class ValueObject extends ComplexType
{
    public ValueObject()
    {
        super();
    }

    public ValueObject(String name)
    {
        super(name);
    }

    public List<String> validate()
    {
        List<String> errors = super.validate();

        for (Field field : resolveFields(true))
        {
            if (!field.isReadOnly() && !field.isList())
            {
                //errors.add("Value object \"" + getName() + "\" has writable property \"" + field.getName() + "\"");
            }
        }
        return errors;
    }

    public List<FieldSet> constructors()
    {
        List<FieldSet> fieldSets = new ArrayList<FieldSet>();
        fieldSets.add(new FieldSet(resolveFields(true)));
        return fieldSets;
    }
}