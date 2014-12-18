package net.sf.xapp.examples.school;

import java.io.File;

import net.sf.xapp.utils.ant.AntFacade;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class TestBase {
    protected TestNode node;

    @Rule
    public TestRule setUpAndTearDown = new TestRule() {
        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    setUp(description);
                    base.evaluate();
                    tearDown();
                }
            };
        }
    };
    protected String backUpDir;

    public void setUp(Description description) throws Exception
    {
        backUpDir = "_OBJ_SERVER_UNIT_TESTS/" + description.getMethodName();
        new AntFacade().deleteDir(new File(backUpDir));

        node = new TestNode(backUpDir, "/spring/basic-node.xml",
                "/spring/channels.xml",
                "/spring/connection-server.xml",
                "/spring/admin-server.xml",
                //"/spring/test.xml",
                "/spring/node-0.xml");


    }

    public void tearDown() throws Exception
    {
        node.getAppContext().destroy();
    }

    protected TestObjClient createClient(String userId) throws InterruptedException {
        return createClient(userId, "11375");
    }

    protected TestObjClient createClient(String userId, String s) throws InterruptedException {
        TestObjClient testClient_1 = new TestObjClient(new File(backUpDir), userId, "school", "s1", s);
        testClient_1.waitUntilInitialized();
        return testClient_1;
    }
}
