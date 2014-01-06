/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling.dummy;


public class AnotherDummy
{
    private String m_varB;
    private int m_varA;


    public AnotherDummy()
    {
    }

    public AnotherDummy(String varB, int varA)
    {
        this.m_varB = varB;
        this.m_varA = varA;
    }


    public String getVarB()
    {
        return m_varB;
    }

    public void setVarB(String varB)
    {
        this.m_varB = varB;
    }

    public int getVarA()
    {
        return m_varA;
    }

    public void setVarA(int varA)
    {
        this.m_varA = varA;
    }

    public boolean equals(Object object)
    {
        if (object instanceof AnotherDummy)
        {
            AnotherDummy d = (AnotherDummy) object;
            return m_varA == d.getVarA() &&
                    m_varB.equals(d.getVarB());
        }

        return false;
    }
}
