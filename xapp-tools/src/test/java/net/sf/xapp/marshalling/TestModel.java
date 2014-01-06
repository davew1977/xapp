/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling;

import net.sf.xapp.annotations.objectmodelling.ListType;

import java.util.List;

public class TestModel
{
    private List<TestEnum> m_enums;

    @ListType(TestEnum.class)
    public List<TestEnum> getEnums()
    {
        return m_enums;
    }

    public void setEnums(List<TestEnum> enums)
    {
        m_enums = enums;
    }
}
