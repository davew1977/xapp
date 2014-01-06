/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.core;

import junit.framework.TestCase;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.difftracking.DiffSet;
import net.sf.xapp.objectmodelling.difftracking.PropertyDiff;
import net.sf.xapp.objectmodelling.difftracking.ReferenceListDiff;
import net.sf.xapp.objectmodelling.difftracking.NewNodeDiff;
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;

public class ClassModelDiffTest extends TestCase
{
    public void testDiff()
    {
        /*DiffSet diffSet = diffset("Database.xml", "Database.xml");
        assertTrue(diffSet.isEmpty());*/
        DiffSet diffSet = diffset("Database.xml", "Database_2.xml");
        System.out.println(Marshaller.toXML(diffSet));
        assertEquals(3,diffSet.getPropertyDiffs().size());
        PropertyDiff propertyDiff = diffSet.getPropertyDiffs().get(1);
        assertEquals("Person", propertyDiff.getClazz());
        assertEquals("Bill", propertyDiff.getKey());
        assertEquals("0", propertyDiff.getOldValue());
        assertEquals("2", propertyDiff.getNewValue());
        assertEquals("Age", propertyDiff.getProperty());
        PropertyDiff richTextPropDiff = diffSet.getPropertyDiffs().get(2);
        assertEquals("Ed", richTextPropDiff.getKey());
        assertEquals("boring", richTextPropDiff.getNewValue());

        assertEquals(2,diffSet.getReferenceListDiffs().size());
        ReferenceListDiff refListDiff = diffSet.getReferenceListDiffs().get(0);
        assertEquals("[Cynthia-Conny]" ,String.valueOf(refListDiff.getAddedNodes()));
        assertEquals("[1]" ,String.valueOf(refListDiff.getAddedNodeIndexes()));

        refListDiff = diffSet.getReferenceListDiffs().get(1);
        assertEquals("[Cynthia-Conny, Andy]" ,String.valueOf(refListDiff.getAddedNodes()));
        assertEquals("Group" ,refListDiff.getContainerClass());
        assertEquals("cooking club" ,refListDiff.getContainerKey());
        assertEquals("Members" ,refListDiff.getListProperty());
        assertEquals("[Cynthia, Bill, Doris]" ,String.valueOf(refListDiff.getRemovedNodes()));

        assertEquals(1, diffSet.getNewNodeDiffs().size());
        NewNodeDiff nnd = diffSet.getNewNodeDiffs().get(0);
        assertEquals("Person", nnd.getNodeClass());
        assertEquals("Database", nnd.getContainerClass());
        assertEquals("People", nnd.getListProperty());
        assertEquals("<Person age=\"0\" name=\"Cynthia-Conny\">\n" +
                "  <Description>]ATADC]!>the most beautiful woman in the world\n" +
                "        , no really!>]]</Description>\n" +
                "</Person>", nnd.getNewValue().trim());
        assertNull(nnd.getContainerKey());

        //test merge
        ClassDatabase<Database> cdb = loadDatabase("Database.xml");
        Database mergeInstance = cdb.getRootInstance();
        cdb.getRootClassModel().merge(mergeInstance, diffSet);
        Database controlInstance = loadDatabase("Database_2.xml").getRootInstance();
        //the following commented out test does not work because the merge does not guarantee
        //that the merged object will match the order
        String realString = Marshaller.toXML(controlInstance);
        String mergeString = Marshaller.toXML(mergeInstance);
        assertEquals(realString, mergeString);
    }

    public void testReferenceRemoved()
    {
        DiffSet diffSet = diffset("Database_3.xml", "Database_4.xml");
        String s = Marshaller.toXML(diffSet);
        diffSet = (DiffSet) new Unmarshaller(DiffSet.class).unmarshalString(s);
        System.out.println(s);
        ClassDatabase<Database> cdb = loadDatabase("Database_3.xml");
        cdb.getRootClassModel().merge(cdb.getRootInstance(), diffSet);
    }

    private DiffSet diffset(String f1, String f2)
    {
        ClassDatabase<Database> cdb1 = loadDatabase(f1);
        ClassDatabase<Database> cdb2 = loadDatabase(f2);
        ClassModel cm1 = cdb1.getRootClassModel();
        ClassModel cm2 = cdb2.getRootClassModel();
        DiffSet diffSet = cm1.diff(cm2, cdb1.getRootInstance(), cdb2.getRootInstance());
        return diffSet;
    }


    private ClassDatabase<Database> loadDatabase(String fileName)
    {
        ClassDatabase<Database> cdb = new ClassModelManager(Database.class);
        cdb.getRootUnmarshaller().unmarshal(getClass().getResourceAsStream(fileName));
        return cdb;
    }
}
