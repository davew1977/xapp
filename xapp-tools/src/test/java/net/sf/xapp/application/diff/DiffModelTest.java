/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 */
package net.sf.xapp.application.diff;

import junit.framework.TestCase;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.difftracking.*;

import java.util.List;

public class DiffModelTest extends TestCase
{
    public void testConstructor()
    {
        Unmarshaller un = new Unmarshaller(DiffSet.class);
        DiffSet baseToMine = (DiffSet) un.unmarshal(getClass().getResourceAsStream("BaseToMine.xml"));
        DiffSet baseToTheirs = (DiffSet) un.unmarshal(getClass().getResourceAsStream("BaseToTheirs.xml"));
        DiffSet mineToTheirs = (DiffSet) un.unmarshal(getClass().getResourceAsStream("MineToTheirs.xml"));

        doTestForIllegalArgException(baseToMine, mineToTheirs, baseToTheirs);

        DiffModel diffModel = new DiffModel(baseToMine, baseToTheirs, mineToTheirs);

        assertEquals(6, diffModel.m_propertyChanges);
        assertEquals(1, diffModel.m_newObjects);
        assertEquals(2, diffModel.m_removedObjects);

        assertEquals(6,diffModel.m_conflicts.size());

        //a conflict in a complexPropertyDiff in the sub properties
        Conflict conflict = find(diffModel.m_conflicts, ConflictType.COMPLEX_DIFF_DIFF, "B", "K", "prop1");
        assertNotNull(conflict);
        List<Conflict> subConflicts = conflict.getSubConflicts();
        //property was changed in both to different values
        assertNotNull(find(subConflicts, ConflictType.SIMPLE_DIFF_DIFF, "BO", null, "prop3"));
        //property changed in one only
        assertNull(find(subConflicts, ConflictType.SIMPLE_DIFF_DIFF, "BO", null, "prop1"));
        //property was change in both to same value
        assertNull(find(subConflicts, ConflictType.SIMPLE_DIFF_DIFF, "BO", null, "prop2"));
        //property changed in one only
        assertNull(find(subConflicts, ConflictType.SIMPLE_DIFF_DIFF, "BO", null, "prop4"));
        assertEquals(1, subConflicts.size()); //only one conflict

        //The complex property was removed in 1 but changed in 2
        conflict = find(diffModel.m_conflicts, ConflictType.COMPLEX_REMOVED_DIFF, "B", "K", "prop2");
        assertNotNull(conflict);
        assertNull(conflict.getSubConflicts());
        assertTrue(((ComplexPropertyDiff)conflict.getThisDiff()).isRemoved());
        assertTrue(((ComplexPropertyDiff)conflict.getOtherDiff()).getDiffSet()!=null);

        //The complex property was removed in 2 but changed in 1
        conflict = find(diffModel.m_conflicts, ConflictType.COMPLEX_DIFF_REMOVED, "B", "K", "prop3");
        assertNotNull(conflict);
        assertNull(conflict.getSubConflicts());
        assertTrue(((ComplexPropertyDiff)conflict.getThisDiff()).getDiffSet()!=null);
        assertTrue(((ComplexPropertyDiff)conflict.getOtherDiff()).isRemoved());

        //complex property was added in 1 and 2, but happened to have identical content
        assertNull(find(diffModel.m_conflicts, ConflictType.COMPLEX_NEW_NEW, "B", "K", "prop4"));

        //complex property was added in 1 and 2, but had different content
        conflict = find(diffModel.m_conflicts, ConflictType.COMPLEX_NEW_NEW, "B", "K", "prop5");
        assertNotNull(conflict);
        assertNull(conflict.getSubConflicts());
        assertTrue(((ComplexPropertyDiff)conflict.getThisDiff()).isNew());
        assertTrue(((ComplexPropertyDiff)conflict.getOtherDiff()).isNew());

        //simple property was changed in 1 and 2 to different values
        conflict = find(diffModel.m_conflicts, ConflictType.SIMPLE_DIFF_DIFF, "C", null, "prop1");
        assertNotNull(conflict);
        conflict = find(diffModel.m_conflicts, ConflictType.SIMPLE_DIFF_DIFF, "C", null, "prop2");
        assertNull(conflict);

        conflict = find(diffModel.m_conflicts, ConflictType.SIMPLE_REF_LIST, "Config", null, "prop1");
        assertNotNull(conflict);

    }

    private Conflict find(List<Conflict> conflicts, ConflictType type, String clazz, String key, String prop)
    {
        for (Conflict conflict : conflicts)
        {
            if(conflict.getType().equals(type))
            {
                switch (type)
                {
                case COMPLEX_DIFF_DIFF:
                case COMPLEX_DIFF_REMOVED:
                case COMPLEX_NEW_NEW:
                case COMPLEX_REMOVED_DIFF:
                    ComplexPropertyDiff thisDiff = (ComplexPropertyDiff) conflict.getThisDiff();
                    if(clazz.equals(thisDiff.getClazz()) && key.equals(thisDiff.getKey()) && prop.equals(thisDiff.getProperty()))
                    {
                        return conflict;
                    }
                    break;
                case SIMPLE_DIFF_DIFF:
                    PropertyDiff sdd = (PropertyDiff) conflict.getThisDiff();
                    if(clazz.equals(sdd.getClazz()) && Property.objEquals(key, sdd.getKey()) && prop.equals(sdd.getProperty()))
                    {
                        return conflict;
                    }
                    break;
                case SIMPLE_DIFF_REMOVED:
                    break;
                case SIMPLE_NEW_NEW:
                    break;
                case SIMPLE_REF_LIST:
                    ReferenceListDiff srl = (ReferenceListDiff) conflict.getThisDiff();
                    if(clazz.equals(srl.getContainerClass()) && Property.objEquals(key, srl.getContainerKey()) && prop.equals(srl.getListProperty()))
                    {
                        return conflict;
                    }
                    break;
                case SIMPLE_REMOVED_DIFF:
                    break;
                }
            }
        }
        return null;
    }

    private static void doTestForIllegalArgException(DiffSet baseToMine, DiffSet mineToTheirs, DiffSet baseToTheirs)
    {
        DiffModel diffModel = null;
        try
        {
            diffModel = new DiffModel(baseToMine,null,mineToTheirs);
            assertTrue(false);
        }
        catch (IllegalArgumentException e)
        {
            //fine
        }
        try
        {
            diffModel = new DiffModel(null, baseToTheirs, mineToTheirs);
            assertTrue(false);
        }
        catch (IllegalArgumentException e)
        {
            //fine
        }
    }
}
