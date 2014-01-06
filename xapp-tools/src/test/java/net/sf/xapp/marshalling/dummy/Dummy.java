/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling.dummy;

import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

@ValidImplementations(SubDummy.class)
public class Dummy
{
    private boolean m_varA;
    protected String m_varC;
    private int m_varB;
    private int m_varD;
    private int m_varE;


    public Dummy()
    {
    }

    public Dummy(boolean a, String c, int b, int d, int e)
    {
        m_varA = a;
        m_varB = b;
        m_varC = c;
        m_varD = d;
        m_varE = e;
    }


    public boolean isVarA()
    {
        return m_varA;
    }

    public void setVarA(boolean varA)
    {
        m_varA = varA;
    }

    public String getVarC()
    {
        return m_varC;
    }

    public void setVarC(String varC)
    {
        m_varC = varC;
    }

    public int getVarB()
    {
        return m_varB;
    }

    public void setVarB(int varB)
    {
        m_varB = varB;
    }

    public int getVarD()
    {
        return m_varD;
    }

    public void setVarD(int varD)
    {
        m_varD = varD;
    }

    public int getVarE()
    {
        return m_varE;
    }

    public void setVarE(int varE)
    {
        m_varE = varE;
    }


    public boolean equals(Object object)
    {
        if (object instanceof Dummy)
        {
            Dummy d = (Dummy) object;
            return m_varA == d.isVarA() &&
                    m_varB == d.getVarB() &&
                    m_varC.equals(d.getVarC()) &&
                    m_varD == d.getVarD() &&
                    m_varE == d.getVarE();
        }

        return false;
    }
}
