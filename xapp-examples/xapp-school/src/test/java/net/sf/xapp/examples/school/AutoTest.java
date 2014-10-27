package net.sf.xapp.examples.school;

import java.io.File;
import java.util.Arrays;

import net.sf.xapp.examples.school.model.Pupil;
import net.sf.xapp.examples.school.model.SchoolSystem;
import net.sf.xapp.examples.school.model.TextFile;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.utils.FileUtils;

import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class AutoTest extends TestBase {

    /**
     * scenario 1 (check basic start up):
     * 1) Start client
     * 2) ensure entire state is loaded
     * 3) ensure rev.txt contains 0
     * 4) ensure obj.xml is written
     * 5) do model change (add text file to a pupil)
     * 6) ensure deltas.xml is written
     *
     */
    @Test
    public void testScenario1() throws InterruptedException {


        EntityRepository entityRepository = node.getEntityRepository();
        int i = entityRepository.countEntitiesWithKey("s1");

        assertEquals(4, i);
        assertNotNull(entityRepository.find(ChatApp.class, "s1"));
        assertNotNull(entityRepository.find(Channel.class, "s1"));
        assertNotNull(entityRepository.find(ObjManager.class, "s1"));
        assertNotNull(entityRepository.find(ObjUpdate.class, "s1"));

        File client1Dir = new File(backUpDir, "client_1");
        TestObjClient testClient_1 = new TestObjClient(client1Dir, "100", "school", "s1");
        testClient_1.waitUntilInitialized();

        //check a few facts about the school
        ObjectMeta obj_56 = testClient_1.getCdb().findObjById(56L);
        assertTrue(obj_56.isA(Pupil.class));
        assertEquals("Berwick School", obj_56.getParent().get("name"));
        assertEquals("Hadwin", obj_56.get("secondName"));

        //check the rev file contents
        assertEquals("0", FileUtils.readFile(testClient_1.getRevFile()).split("\n")[0]);
        //check object file exists and can be unmarshalled
        Unmarshaller unmarshaller = new Unmarshaller(SchoolSystem.class);
        ObjectMeta objMeta = unmarshaller.unmarshal(testClient_1.getObjFile());
        //check identical to object retrieved from server
        assertEquals(testClient_1.getObjMeta().toXml(), objMeta.toXml());

        UserId uid = testClient_1.getUserId();
        testClient_1.createEmptyObject(uid, new ObjLoc(59L, "Files", -1), TextFile.class);
        ObjectMeta objectMeta = testClient_1.getCdb().lastCreated();
        assertTrue(objectMeta.isA(TextFile.class));
        testClient_1.updateObject(objectMeta, "Name", "BooBoo");


        testClient_1.close();
    }

}
