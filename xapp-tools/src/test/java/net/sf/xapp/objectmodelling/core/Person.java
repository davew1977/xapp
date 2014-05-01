/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.marshalling.FormattedText;

public class Person
{
    private String m_name;
    private int m_age;
    private Person m_father;
    private Person m_mother;
    private String m_description;

    public int getAge()
    {
        return m_age;
    }

    public void setAge(int age)
    {
        m_age = age;
    }

    @FormattedText
    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String description)
    {
        m_description = description;
    }

    @Key
    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    @Reference
    public Person getFather()
    {
        return m_father;
    }

    public void setFather(Person father)
    {
        m_father = father;
    }

    @Reference
    public Person getMother()
    {
        return m_mother;
    }

    public void setMother(Person mother)
    {
        m_mother = mother;
    }

    @Override
    public String toString()
    {
        return m_name;
    }
}
