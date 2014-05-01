/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ListType;
import net.sf.xapp.annotations.objectmodelling.ContainsReferences;

import java.util.List;

public class Group
{
    private String m_name;
    private List<Person> m_members;
    private List<Asset> m_assets;

    @Key
    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    @ListType(Person.class)
    @ContainsReferences
    public List<Person> getMembers()
    {
        return m_members;
    }

    public void setMembers(List<Person> members)
    {
        m_members = members;
    }

    @ListType(Asset.class)
    public List<Asset> getAssets()
    {
        return m_assets;
    }

    public void setAssets(List<Asset> assets)
    {
        m_assets = assets;
    }

    @Override
    public String toString()
    {
        return m_name;
    }
}
