package net.sf.xapp.examples.school;

import net.sf.xapp.examples.school.model.*;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.objclient.DeltaFile;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ConflictResolution;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Some test cases exercising the clients offline handling
 */
public class OfflineClientInteractionTest extends TestBase {


    /**
     * scenario 6: offline no conflict
     *   1) start a client
     *   2) do a couple of changes
     *   3) go offline
     *   4) make some deltas (create a new object, change a property etc)
     *   5) ensure offline deltas are as expected
     *   5a) make sure first line of offline deltas is good
     *   6) close client
     *   7) start a new client that won't be able to connect (start in offline mode)
     *   8) ensure that previous offline changes are applied to model
     *   9) make some more offline changes
     *   10) ensure good offline file state (should have simply appended new offline changes)
     *   11) go online
     *   12) check that client state and server state are sync'd correctly (compare xml dumps)
     *
     */
    @Test
    public void testScenario6() throws InterruptedException {
        TestObjClient c1 = createClient("100");
        TextFile textFile = c1.create(59L, "Files", TextFile.class);
        textFile.setName("Hello Kitty");
        c1.commit(textFile);

        c1.setOffline();
        Pupil alice = c1.checkout(15L);
        assertEquals("alicew", alice.getUsername());

        alice.setSecondName("Fruit");
        c1.commit(alice);

        DeltaFile offlineFile = c1.getObjClient().getOfflineFile();
        assertEquals(1, offlineFile.getDeltas().size());
        assertEquals(2, offlineFile.getBaseRevision());

        c1.close();

        c1 = createClient("100", "11376"); //deliberately provide bad port so connection fails

        assertFalse(c1.getObjClient().isConnected());
        //ensure previous offline changes are applied to model
        alice = c1.checkout(15L);
        assertEquals("Fruit", alice.getSecondName());
        //make some more offline changes
        PersonSettings personSettings = c1.create(15L, PersonSettings.class);
        Long id = c1.lastCreated().getId();
        Hat hat = c1.create(id, Hat.class);
        hat.setColour(Colour.blue);
        hat.setType(HatType.Bowler);
        c1.commit(hat);

        //ensure good offline file state (should have simply appended new offline changes)
        File rawOfflineFile = c1.getObjClient().getOfflineFile().getFile();
        DeltaFile deltaFile = new DeltaFile(rawOfflineFile);
        assertEquals(2, deltaFile.getBaseRevision());
        assertEquals(4, deltaFile.size());

        c1.close();

        //go online with new client
        c1 = createClient("100");

        LiveObject lo = node.getTarget(ObjUpdate.class, LiveObject.class, "s1");

        //ensure serialized client obj matches serialized server obj
        assertEquals(c1.getObjMeta().toXml(), lo.getRootObj().toXml() );
        alice = c1.checkout(15L);
        assertEquals(Colour.blue, alice.getPersonSettings().getFavouriteHat().getColour());
        c1.close();

    }

    /**
     * this scenario covers when there are offline changes which conflict with server changes
     * 1) start 2 clients
     * 2) c1 makes a change
     * 3) c2 goes offline
     * 4) c1 makes a change to props a and c
     * 5) c2 makes a change to props a and b
     * 6) c2 goes online, no changes should be applied, check that conflict was detected
     * 7) c1 makes a change to b
     * 8) c2 tries to 'apply their' conflicts, make sure the new conflict is detected
     * 9) c2 chooses to apply their conflicts
     */
    @Test
    public void testScenario7() throws InterruptedException {
        TestObjClient c1 = createClient("100");
        TestObjClient c2 = createClient("101");
        Pupil c1_alice = c1.checkout(15L);
        c1_alice.setSecondName("Blom");
        c1.commit(c1_alice);
        c2.waitFor(MessageTypeEnum.ObjListener_PropertiesChanged);
        Pupil c2_alice = c2.checkout(15L);
        assertEquals("Blom", c2_alice.getSecondName());

        c2.setOffline();
        c1_alice = c1.checkout(15L);
        c1_alice.setFirstName("Alicia");
        c1.commit(c1_alice);
        PersonSettings personSettings = c1.create(15L, PersonSettings.class);
        personSettings.setFavouriteWords(new String[]{"gib", "bonnet"});
        c1.commit(personSettings);

        c2_alice = c2.checkout(15L);
        c2_alice.setFirstName("Anastasia");
        c2.commit(c2_alice);
        c2_alice = c2.checkout(15L);
        c2_alice.setSecondName("Di Canio");
        c2.commit(c2_alice);

        c2.connect();//should block until conflicts are received
        assertEquals(1, c2.getConflicts().propConflicts.size());
        assertEquals("Anastasia", c2.getConflicts().propConflicts.get(0).getMine().getNewValue());
        assertEquals("Alicia", c2.getConflicts().propConflicts.get(0).getTheirs().getNewValue());


        c1_alice = c1.checkout(15L);
        c1_alice.setSecondName("Philips");
        c1.commit(c1_alice);

        c2.applyChanges(ConflictResolution.FORCE_ALL_MINE);
        c2.waitFor(MessageTypeEnum.ObjManagerReply_ApplyChangesResponse);

        c1.close();
        c2.close();
    }

    @Test
    public void testScenario8() {

    }
}
