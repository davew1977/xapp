/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling;

import junit.framework.TestCase;

import java.io.*;

import net.sf.xapp.marshalling.dummy.Dummy;
import net.sf.xapp.marshalling.dummy.DummyModel;
import net.sf.xapp.marshalling.dummy.AnotherDummy;
import net.sf.xapp.objectmodelling.core.ClassModelManager;


public class MarshallUnmarshallTest extends TestCase
{
    private StringWriter m_resultVerification;
    private Marshaller m_formatedMarshaller;
    private Marshaller m_marshaller;
    private Unmarshaller m_unmarshaller;
    private ClassModelManager m_cmm;

    private static final boolean FORMATTED = true;
    private static final boolean NOT_FORMATTED = false;
    private static final String TEST_RESULT_FILE_NAME = "MarshallUnmarshallTestOutput.xml";

    protected void setUp() throws Exception
    {
        m_cmm = new ClassModelManager(DummyModel.class);
        m_resultVerification = new StringWriter();

        m_formatedMarshaller = new Marshaller(DummyModel.class, m_cmm, FORMATTED);
        m_marshaller = new Marshaller(DummyModel.class, m_cmm, NOT_FORMATTED);
        m_unmarshaller = new Unmarshaller(m_cmm.getClassModel(DummyModel.class));
    }


    /**
     *  Read the same model info from two files, one ordered and one unordered, and make sure that the written output is
     * the same in both cases.
     */
    public void testUnmarshallMarshall()
    {
        verifyUnmarshallMarshall("dummy/unorderedSimple.xml", "dummy/expectedSimple.xml");
        verifyUnmarshallMarshall("dummy/unordered.xml", "dummy/expected.xml");        
    }

    private void verifyUnmarshallMarshall(String unorderedFilePath, String expectedFilePath)
    {
        // Make sure we stat with an empty StringWriter
        m_resultVerification = new StringWriter();

        DummyModel unorderedModel = (DummyModel) m_unmarshaller.unmarshal(getClass().getResourceAsStream(unorderedFilePath));
        DummyModel expectedModel = (DummyModel) m_unmarshaller.unmarshal(getClass().getResourceAsStream(expectedFilePath));

        String unordered;
        String expected;
        m_marshaller.marshal(m_resultVerification, unorderedModel);
        unordered = m_resultVerification.toString();

        m_resultVerification = new StringWriter();
        m_marshaller.marshal(m_resultVerification, expectedModel);
        expected = m_resultVerification.toString();

        assertEquals("Expecting both unordered and ordered input to give the same output.", unordered, expected);}


    /**
     * Writes a predefined model to disc and then reads it. The resulting object model should be the same as the one we
     * started with.
     */
    public void testReadAfterWrite()
    {
        // Create our expected model
        DummyModel expectedModel = new DummyModel();
        Dummy dummy = new Dummy(true, "hej", 1, 2, 3);
        AnotherDummy anotherDummy = new AnotherDummy("hej igen", 1020);
        expectedModel.setDummy(dummy);
        expectedModel.setAnotherDummy(anotherDummy);

        // Marshall the expected model to disc and then read it back into a new model
        m_formatedMarshaller.marshal(TEST_RESULT_FILE_NAME, expectedModel);
        DummyModel readModel = (DummyModel) m_unmarshaller.unmarshal(TEST_RESULT_FILE_NAME);

        // Assert that they are equal
        assertEquals(expectedModel.getDummy(), readModel.getDummy());
        assertEquals(expectedModel.getAnotherDummy(), readModel.getAnotherDummy());
    }

    /**
     *  Reads an expected and correct xml from disc. Then we write it back and makes sure it stay the same.
     * @throws java.io.FileNotFoundException
     */
    public void testWriteAfterRead() throws FileNotFoundException
    {
        // Read the values from disc and parse
        DummyModel model = (DummyModel) m_unmarshaller.unmarshal(getClass().getResourceAsStream("dummy/expectedSimple.xml"));
        m_formatedMarshaller.marshal(TEST_RESULT_FILE_NAME, model);

        // Read the saved file form disc and compare it to the expected result.
        File savedFile = new File(TEST_RESULT_FILE_NAME);
        String expect = getXMLStringFromInputStream(getClass().getResourceAsStream("dummy/expectedSimple.xml"));
        String saved = getXMLStringFromInputStream(new FileInputStream(savedFile));
        assertEquals("Saved file is not the same as the original one.", expect, saved);

        // Clean up
        savedFile.delete();
    }

    /**
     * Returns the string representation of the xml file pointed to by the InputStream
     * @param is
     * @return
     */
    private String getXMLStringFromInputStream(InputStream is)
    {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();
        try
        {
            while ((line = br.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            br.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
