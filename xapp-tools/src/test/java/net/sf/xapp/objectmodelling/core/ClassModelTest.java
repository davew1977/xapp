/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.core;

import junit.framework.TestCase;
import net.sf.xapp.objectmodelling.api.ClassDatabase;

import java.util.Vector;

public class ClassModelTest extends TestCase
{
    private ClassDatabase m_classDatabase;
    private ClassModel m_classModel;

    public void testDelete()
    {
        Database database = loadDatabase();
        ClassModel assetClassModel = m_classDatabase.getClassModel(Asset.class);
        ClassModel groupClassModel = m_classDatabase.getClassModel(Group.class);
        ClassModel personClassModel = m_classDatabase.getClassModel(Person.class);
        int noPeopleBefore = personClassModel.getAllInstancesInHierarchy().size();
        Vector<Object> assets = assetClassModel.getAllInstancesInHierarchy();
        //check there are 3 assets and 2 groups,
        assertEquals(4, assets.size());
        assertEquals(3, groupClassModel.getAllInstancesInHierarchy().size());
        Group group = database.getGroups().get(0);
        //try deleting group 1
        groupClassModel.delete(group);
        //check assets and group are gone, but number of people is same
        assertEquals(2, groupClassModel.getAllInstancesInHierarchy().size());
        assets = assetClassModel.getAllInstancesInHierarchy();
        assertEquals(2, assets.size());
        assertEquals(noPeopleBefore, personClassModel.getAllInstancesInHierarchy().size());
    }

    private Database loadDatabase()
    {
        m_classDatabase = new ClassModelManager(Database.class);
        m_classModel = m_classDatabase.getRootClassModel();
        return (Database) m_classDatabase.getRootUnmarshaller().unmarshal(getClass().getResourceAsStream("Database.xml"));
    }


}
