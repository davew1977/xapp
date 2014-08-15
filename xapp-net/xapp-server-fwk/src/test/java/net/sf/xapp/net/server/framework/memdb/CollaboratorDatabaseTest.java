package net.sf.xapp.net.server.framework.memdb;

import net.sf.xapp.net.common.framework.LispObj;
import net.sf.xapp.net.common.types.Clause;
import net.sf.xapp.net.common.types.QueryData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * UserEntityWrapper: davidw
 * Date: 2/19/14
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollaboratorDatabaseTest {

    @Test
    public void testLoad()
    {
        final List<String> props = Arrays.asList("sex", "age", "faveGame");
        Database<LispObj> db = new CollaboratorDatabase<LispObj>(new PropertyInspector<LispObj>()
        {
            @Override
            public String getValue(LispObj item, String property)
            {
                return item.get(props.indexOf(property));
            }
        }, props);

        db.store("1", new LispObj("male,0-17,monopoly"));
        db.store("2", new LispObj("male,0-17,monopoly"));
        db.store("3", new LispObj("male,18-34,monopoly"));
        db.store("4", new LispObj("female,18-34,golf"));
        db.store("5", new LispObj("male,35+,monopoly"));
        db.store("6", new LispObj("male,35+,chess"));
        LispObj maleGolfer = new LispObj("male,35+,golf");
        db.store("7", maleGolfer);
        db.store("8", new LispObj("female,35+,golf"));
        db.store("9", new LispObj("female,35+,golf"));
        db.store("10", new LispObj("female,35+,golf"));
        db.store("11", new LispObj("female,0-17,golf"));

        //select all males
        assertEquals(6, db.find("[[[sex,[male]]],[],false]").size());
        //select all females
        assertEquals(5, db.find("[[[sex,[female]]],[],false]").size());
        //select all females who are 0-17 or 35+
        assertEquals(4, db.find("[[[sex,[female]],[age,[0-17,35+]]],[],false]").size());
        //select all men who like golf
        assertEquals(1, db.find("[[[sex,[male]],[faveGame,[golf]]],[],false]").size());

        //select all
        assertEquals(11, db.find(new QueryData(new ArrayList<Clause>(), new ArrayList<String>(), true)).size());
        assertEquals(0, db.find(new QueryData(new ArrayList<Clause>(), new ArrayList<String>(), false)).size());

        db.remove("7");
        assertEquals(0, db.find("[[[sex,[male]],[faveGame,[golf]]],[],false]").size());

        assertNull(db.remove("7"));

    }
}
