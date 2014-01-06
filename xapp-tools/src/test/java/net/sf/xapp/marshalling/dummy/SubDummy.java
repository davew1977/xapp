/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling.dummy;

public class SubDummy extends Dummy
{
    private int m_id;


    public SubDummy()
    {
    }

    public SubDummy(int id, boolean a, String c, int b, int d, int e)
    {
        super(a, c, b, d, e);
        m_id = id;
    }

    public int getId()
    {
        return m_id;
    }

    public void setId(int id)
    {
        this.m_id = id;
    }
}
