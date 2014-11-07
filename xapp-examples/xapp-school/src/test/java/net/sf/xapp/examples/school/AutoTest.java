package net.sf.xapp.examples.school;

import java.io.File;
import java.util.List;

import net.sf.xapp.examples.school.model.PersonSettings;
import net.sf.xapp.examples.school.model.Pupil;
import net.sf.xapp.examples.school.model.SchoolSystem;
import net.sf.xapp.examples.school.model.TextFile;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.LiveObject;
import net.sf.xapp.objserver.apis.objlistener.to.PropertiesChanged;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.to.GetDeltasResponse;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.utils.FileUtils;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

        scenario1_internal();
    }

    /**
     * scenario 2 (check that save resets the saved state)
     * 1) do all of scenario 1
     * 2) call "save()" in the client app
     * 3) ensure there's no delta file, obj.xml is written, and rev.txt is at 1
     */
    @Test
    public void testScenario2() throws InterruptedException {
        TestObjClient client = scenario1_internal();
        client.getObjClient().save();

        ensureNoDeltaFileAndRevAt1(client);
    }

    /**
     * scenario 3 (start up after hard client shutdown - where app not saved)
     * 1) do all of scenario 1
     * 2) kill client
     * 3) restart client
     * 4) ensure client queries for deltas since 1
     * 5) ensure server send succes response with no deltas
     * 6) ensure client applies the locally saved deltas
     * 7) ensure client resets the state (rev.txt at 1 and no deltas file)
     * @throws InterruptedException
     */
    @Test
    public void testScenario3() throws InterruptedException  {
        scenario1_internal();
        TestObjClient testClient_1 = createClient("100");

        GetDeltasResponse getDeltasResponse = testClient_1.waitFor(MessageTypeEnum.ObjManagerReply_GetDeltasResponse);
        assertTrue(getDeltasResponse.getDeltas().isEmpty());
        assertNull(getDeltasResponse.getErrorCode());

        LiveObject lo = node.getTarget(ObjUpdate.class, LiveObject.class, "s1");

        //ensure serialized client obj matches serialized server obj
        assertEquals(testClient_1.getObjMeta().toXml(), lo.getRootObj().toXml());

        ensureNoDeltaFileAndRevAt1(testClient_1);
    }

    /**
     * scenario 4 (start up after hard shutdown when server deltas occur)
     * 1) join 2 clients
     * 2) do model change from client 1 (add text file to pupil)
     * 3) ensure both clients deltas files contain 1 delta
     * 4) hard shutdown client 1
     * 5) do 2 model changes from client 2 (add another image file to same user, change text in first text file)
     * 6) restart client 1
     * 7) ensure local deltas are applied
     * 8) ensure server was asked for deltas since 1
     * 9) ensure server sends deltas 2 and 3
     * 10) ensure that deltas 1,2 and 3 are applied to the client
     * 11) make a 4th model change (move the second img file up in the list)
     * 12) client 1 deltas file should contain delta 4 only, rev.txt should be set to 3
     * 13) ensure client 2 deltas file contains all 4 deltas, rev.txt should be 0
     */
    @Test
    public void testScenario4() throws InterruptedException {
        TestObjClient c1 = createClient("100");
        TestObjClient c2 = createClient("101");
        ObjectMeta chocky = c1.createEmptyObject(new ObjLoc(45L, "People", -1), Pupil.class);
        assertTrue(chocky.isA(Pupil.class));
        c1.updateObject(chocky, "Username", "Chocky");
        c1.updateObject(chocky, "FirstName", "Chocky");
        c1.updateObject(chocky, "SecondName", "Chimp");
        ObjectMeta personSettings = c2.createEmptyObject(new ObjLoc(chocky.getId(), "PersonSettings", -1), PersonSettings.class);
        c1.updateObject(personSettings, "FavouriteWords", "Nice,Pork Chop,Dance");
        c2.waitFor(MessageTypeEnum.ObjListener_PropertiesChanged, "Rev", 6L);
        PersonSettings settings = (PersonSettings) c2.getCdb().findObjById(personSettings.getId()).getInstance();
        assertEquals("Pork Chop", settings.getFavouriteWords()[1]);

        c1.close();
        ObjectMeta textFile = c2.createEmptyObject(new ObjLoc(88L, "Files", -1), TextFile.class);
        c2.updateObject(textFile, "Text", "This is a text doc about chocky");

        assertEquals(6, c1.readDeltas().size()); //this client missed 2 deltas
        assertEquals(8, c2.readDeltas().size());

        c1 = createClient("100");
        GetDeltasResponse response = c1.waitFor(MessageTypeEnum.ObjManagerReply_GetDeltasResponse);
        assertEquals(2, response.getDeltas().size());
        assertEquals(c1.getObjMeta().toXml(), c2.getObjMeta().toXml());

        //make one last change
        c1.updateObject(textFile, "Name", "My work");
        assertEquals(1, c1.readDeltas().size());
        assertEquals(9, c2.readDeltas().size());
    }

    private TestObjClient scenario1_internal() throws InterruptedException {
        EntityRepository entityRepository = node.getEntityRepository();
        int i = entityRepository.countEntitiesWithKey("s1");

        assertEquals(4, i);
        assertNotNull(entityRepository.find(ChatApp.class, "s1"));
        assertNotNull(entityRepository.find(Channel.class, "s1"));
        assertNotNull(entityRepository.find(ObjManager.class, "s1"));
        assertNotNull(entityRepository.find(ObjUpdate.class, "s1"));

        TestObjClient testClient_1 = createClient("100");

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

        testClient_1.createEmptyObject(new ObjLoc(59L, "Files", -1), TextFile.class);
        ObjectMeta objectMeta = testClient_1.lastCreated();
        assertTrue(objectMeta.isA(TextFile.class));
        testClient_1.updateObject(objectMeta, "Name", "BooBoo");

        List<Delta> deltas = testClient_1.readDeltas();
        assertEquals(2, deltas.size());
        PropertiesChanged update = (PropertiesChanged) deltas.get(1).getMessage();
        assertEquals((Object) 87L, update.getChangeSets().get(0).getObjId());
        assertEquals("Name", update.getChangeSets().get(0).getChanges().get(0).getProperty());
        assertEquals("BooBoo", update.getChangeSets().get(0).getChanges().get(0).getNewValue());

        assertEquals("BooBoo", objectMeta.get("Name"));

        testClient_1.close();
        return testClient_1;
    }

    private void ensureNoDeltaFileAndRevAt1(TestObjClient client) {
        assertTrue(!client.getDeltaFile().exists());
        assertEquals("2", FileUtils.readFile(client.getRevFile()).split("\n")[0]);
        Unmarshaller unmarshaller = new Unmarshaller(SchoolSystem.class);
        ObjectMeta objMeta = unmarshaller.unmarshal(client.getObjFile());
        //check identical to object retrieved from server
        assertEquals(client.getObjMeta().toXml(), objMeta.toXml());

        assertEquals("BooBoo", objMeta.getClassDatabase().findObjById(87L).get("Name"));
    }

    private TestObjClient createClient(String userId) throws InterruptedException {
        TestObjClient testClient_1 = new TestObjClient(new File(backUpDir), userId, "school", "s1");
        testClient_1.waitUntilInitialized();
        return testClient_1;
    }
}
