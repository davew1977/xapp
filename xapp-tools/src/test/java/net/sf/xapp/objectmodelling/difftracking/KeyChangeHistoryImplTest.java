/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.objectmodelling.difftracking;

import junit.framework.TestCase;
import net.sf.xapp.marshalling.Unmarshaller;

public class KeyChangeHistoryImplTest extends TestCase
{
    public void testResolveKey()
    {
        KeyChangeHistory k = new KeyChangeHistoryImpl();
        ChangeModel cm = (ChangeModel) new Unmarshaller(ChangeModel.class).unmarshal(getClass().getResourceAsStream("TestChangeModel.xml")).getInstance();
        k.init(cm);
        //test model 1 has a chain of 4 changes involving the same key
        assertNull(k.resolveKey("A","a"));
        assertEquals("n", k.resolveKey("A", "j"));
        assertEquals("n", k.resolveKey("A", "k"));
        assertEquals("n", k.resolveKey("A", "l"));
        assertEquals("n", k.resolveKey("A", "m"));

        k = new KeyChangeHistoryImpl();
        cm = (ChangeModel) new Unmarshaller(ChangeModel.class).unmarshal(getClass().getResourceAsStream("TestChangeModel2.xml")).getInstance();
        k.init(cm);
        //test model 2 has a chain of 3 changes involving the same key, with a changeset in the
        //middle where this key is not changed
        assertNull(k.resolveKey("A","a"));
        assertEquals("n", k.resolveKey("A", "j"));
        assertEquals("n", k.resolveKey("A", "k"));
        assertNull(k.resolveKey("A", "l"));
        assertEquals("n", k.resolveKey("A", "m"));
    }
}
