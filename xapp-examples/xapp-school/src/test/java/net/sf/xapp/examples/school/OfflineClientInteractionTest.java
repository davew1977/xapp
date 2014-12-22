package net.sf.xapp.examples.school;

import net.sf.xapp.examples.school.model.*;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objclient.DeltaFile;
import net.sf.xapp.objclient.OfflineMeta;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.to.PropertiesChanged;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.to.GetDeltasResponse;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.utils.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

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

        c1.getObjClient().setOffline();
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


}
