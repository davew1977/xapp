/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.difftracking;

import junit.framework.TestCase;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.marshalling.Marshaller;

public class KeyChangeDictionaryImplTest extends TestCase
{
    public void testRegister()
    {
        //test that new objects do not cause key changes to be added
        KeyChangeDictionaryImpl k = new KeyChangeDictionaryImpl();
        k.primaryKeyChange(new PrimaryKeyChange("A", null, "1"));
        k.primaryKeyChange(new PrimaryKeyChange("A", null, "2"));
        k.primaryKeyChange(new PrimaryKeyChange("A", null, "3"));

        assertTrue(k.getPrimaryKeyChanges().get("A").isEmpty());

        //check that a normal change is registered
        PrimaryKeyChange change1 = new PrimaryKeyChange("A", "3", "6");
        k.primaryKeyChange(change1);
        assertEquals(change1, k.getPrimaryKeyChanges().get("A").get("6"));

        //check a normal change is removed
        k.objectRemoved("A", "6");
        assertTrue(k.getPrimaryKeyChanges().get("A").isEmpty());

        //check that a second change removes the first
        k.primaryKeyChange(new PrimaryKeyChange("A", "3", "6"));
        k.primaryKeyChange(new PrimaryKeyChange("A", "6", "4"));
        assertEquals(1, k.getPrimaryKeyChanges().get("A").size());
        assertEquals("4", k.getPrimaryKeyChanges().get("A").get("4").getNew());
        assertEquals("3", k.getPrimaryKeyChanges().get("A").get("4").getOld());

        //check that several changes that result in the initial value being returned to will
        //result in no change being registered
        k.primaryKeyChange(new PrimaryKeyChange("A", "4", "8"));
        k.primaryKeyChange(new PrimaryKeyChange("A", "8", "9"));
        k.primaryKeyChange(new PrimaryKeyChange("A", "9", "10"));
        assertEquals(1, k.getPrimaryKeyChanges().get("A").size());
        k.primaryKeyChange(new PrimaryKeyChange("A", "10", "3"));
        assertEquals(0, k.getPrimaryKeyChanges().get("A").size());
 
    }

    public void testFindByOldAndNew()
    {
        ChangeModel cm = (ChangeModel) new Unmarshaller(ChangeModel.class).unmarshal(getClass().getResourceAsStream("TestChangeModel.xml"));
        KeyChangeDictionary k = new KeyChangeDictionaryImpl();
        k.init(cm.getChangeSets().get(0));
        assertEquals("k", k.findByOld("A", "j").getNew());
        assertEquals("k", k.findByNew("A", "k").getNew());
        assertNull(k.findByOld("A", "x"));
    }

    public void testOldAndNewKeys()
    {
        KeyChangeDictionaryImpl k = new KeyChangeDictionaryImpl();
        k.primaryKeyChange(new PrimaryKeyChange("A", null, "8",true));
        k.primaryKeyChange(new PrimaryKeyChange("A", null, "8",true));

        ChangeSet changeSet = k.createChangeSet();
        //check only one entry added
        assertEquals(1, changeSet.getNewKeyChanges().size());
        assertEquals(new NewKey("A", "8"), changeSet.getNewKeyChanges().get(0));
        assertEquals(0, changeSet.getRemovedKeyChanges().size());

        k.objectRemoved("A","8",true);
        //check empty when new key was removed
        changeSet = k.createChangeSet();
        assertEquals(0,changeSet.getRemovedKeyChanges().size());
        assertEquals(0,changeSet.getNewKeyChanges().size());

        k.objectRemoved("A", "7", true);
        changeSet = k.createChangeSet();
        //check removed objects registered
        assertEquals(1,changeSet.getRemovedKeyChanges().size());
        assertEquals(0,changeSet.getNewKeyChanges().size());
        assertEquals(new RemovedKey("A","7"),changeSet.getRemovedKeyChanges().get(0));

        k.primaryKeyChange(new PrimaryKeyChange("A", null, "7",true));
        changeSet = k.createChangeSet();
        //check nothing registered when a removed obj is readded
        assertEquals(0,changeSet.getRemovedKeyChanges().size());
        assertEquals(0,changeSet.getNewKeyChanges().size());

        k.objectRemoved("A","6");
        changeSet = k.createChangeSet();
        //check nothing changes when flag not set
        assertEquals(0,changeSet.getRemovedKeyChanges().size());
        assertEquals(0,changeSet.getNewKeyChanges().size());

        k.primaryKeyChange(new PrimaryKeyChange("A", null, "2",false));
        changeSet = k.createChangeSet();
        //check nothing changes when flag not set
        assertEquals(0,changeSet.getRemovedKeyChanges().size());
        assertEquals(0,changeSet.getNewKeyChanges().size());

        print(changeSet);

    }

    private void print(ChangeSet changeSet)
    {
        Marshaller m = new Marshaller(ChangeSet.class);
        System.out.println(m.toXMLString(changeSet));
    }
}
