/*
 *
 * Date: 2010-sep-20
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import java.util.List;

/**
 * a type that can be stored in a lobby
 */
public class LobbyType extends ComplexType
{
    @Override
    public List<String> validate()
    {
        List<String> errors = super.validate();
        for (Field field : resolveFields(true))
        {
            if(field.getType() instanceof ComplexType)
            {
                errors.add(String.format("%s is not a primitive or enum", field));
            }
        }
        return errors;
    }
}
