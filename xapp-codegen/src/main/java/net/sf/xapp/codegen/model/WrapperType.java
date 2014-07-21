/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Hide;

public class WrapperType extends AbstractType
{

    @Override
    @Hide
    public boolean isSkipGeneration()
    {
        return true;
    }
}