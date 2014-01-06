/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling.dummy;

public class DummyModel
{
    private Dummy m_dummy;
    private AnotherDummy m_anotherDummy;


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
}
