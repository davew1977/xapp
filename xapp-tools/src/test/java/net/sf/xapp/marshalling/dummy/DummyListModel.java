/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling.dummy;

import net.sf.xapp.annotations.objectmodelling.ListType;

import java.util.List;


public class DummyListModel
{
    private List<Dummy> m_list;


    @ListType(Dummy.class)
    public List<Dummy> getList()
    {
        return m_list;
    }

    public void setList(List<Dummy> list)
    {
        m_list = list;
    }
}
