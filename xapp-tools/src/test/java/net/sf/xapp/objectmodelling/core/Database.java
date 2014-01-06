/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.objectmodelling.ListType;
import net.sf.xapp.application.api.Launcher;

import java.util.List;

public class Database
{
    private List<Person> m_people;
    private List<Group> m_groups;

    @ListType(Person.class)
    public List<Person> getPeople()
    {
        return m_people;
    }

    public void setPeople(List<Person> people)
    {
        m_people = people;
    }

    @ListType(Group.class)
    public List<Group> getGroups()
    {
        return m_groups;
    }

    public void setGroups(List<Group> groups)
    {
        m_groups = groups;
    }

    @Override
    public String toString()
    {
        return "Database";
    }

    public static void main(String[] args)
    {
        Launcher.run(Database.class, "Database.xml");
    }
}
