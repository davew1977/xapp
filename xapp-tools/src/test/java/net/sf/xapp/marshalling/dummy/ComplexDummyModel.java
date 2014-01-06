package net.sf.xapp.marshalling.dummy;

import net.sf.xapp.annotations.objectmodelling.ListType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bjorna
 * Date: Sep 30, 2008
 * Time: 10:39:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComplexDummyModel
{
    private Dummy m_dummy;
    private AnotherDummy m_anotherDummy;
    private List<Dummy> m_list;


    public Dummy getDummy()
    {
        return m_dummy;
    }

    public void setDummy(Dummy dummy)
    {
        m_dummy = dummy;
    }

    public AnotherDummy getAnotherDummy()
    {
        return m_anotherDummy;
    }

    public void setAnotherDummy(AnotherDummy anotherDummy)
    {
        m_anotherDummy = anotherDummy;
    }

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
