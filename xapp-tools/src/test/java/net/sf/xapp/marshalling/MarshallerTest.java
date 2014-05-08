/**
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 */
package net.sf.xapp.marshalling;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.marshalling.dummy.Dummy;
import net.sf.xapp.marshalling.dummy.DummyListModel;
import net.sf.xapp.marshalling.dummy.SubDummy;

public class MarshallerTest extends TestCase
{
    private StringWriter m_resultVerification;
    private Marshaller m_marshaller;

    private static final boolean NOT_FORMATTED = false;

    protected void setUp() throws Exception
    {        
        m_resultVerification = new StringWriter();
        m_marshaller = new Marshaller(Dummy.class, new ClassModelManager(Dummy.class), NOT_FORMATTED);
    }

    /**
     * Makes sure that the list order is the same in the marshall output as the order the items were
     * put into the list. Only testing lists containing one object type.
     */
    public void testListOrderSameType()
    {
        DummyListModel listModel = new DummyListModel();
        List<Dummy> list = new ArrayList<Dummy>();

        // Add three dummies to the list and set it on the model
        list.add(new Dummy(true,"dummy2", 1, 15, 11));
        String expected1 = "<Dummy varA=\"true\" varB=\"1\" varC=\"dummy2\" varD=\"15\" varE=\"11\"/>";

        list.add(new Dummy(false,"dummy3", 2, 16, 12));
        String expected2 = "<Dummy varB=\"2\" varC=\"dummy3\" varD=\"16\" varE=\"12\"/>";

        list.add(new Dummy(true,"dummy", 1000, 14, 10));
        String expected3 = "<Dummy varA=\"true\" varB=\"1000\" varC=\"dummy\" varD=\"14\" varE=\"10\"/>";

        listModel.setList(list);

        // Marshall the model and verify the result
        m_marshaller = new Marshaller(DummyListModel.class, new ClassModelManager(DummyListModel.class), NOT_FORMATTED);
        m_marshaller.marshal(m_resultVerification, listModel);
        assertEquals("Marshalled xml does not match the expected output",
                "<DummyListModel><List>"+expected1 + expected2 + expected3+"</List></DummyListModel>",
                m_resultVerification.toString());
    }

    /**
     * Makes sure that the list order is the same in the marshall output as the order the items were
     * put into the list. Testing a list containgin more than one object type.
     */
    public void testListOrderTwoTypes()
    {
        DummyListModel listModel = new DummyListModel();
        List<Dummy> list = new ArrayList<Dummy>();

        list.add(new Dummy(true,"dummy2", 1, 15, 11));
        String expected1 = "<Dummy varA=\"true\" varB=\"1\" varC=\"dummy2\" varD=\"15\" varE=\"11\"/>";

        list.add(new SubDummy(13, true,"subDummy", 2, 12, 110));
        String expected2 = "<SubDummy id=\"13\" varA=\"true\" varB=\"2\" varC=\"subDummy\" varD=\"12\" varE=\"110\"/>";

        list.add(new Dummy(true,"dummy", 1000, 14, 10));
        String expected3 = "<Dummy varA=\"true\" varB=\"1000\" varC=\"dummy\" varD=\"14\" varE=\"10\"/>";

        listModel.setList(list);

        m_marshaller = new Marshaller(DummyListModel.class, new ClassModelManager(DummyListModel.class), NOT_FORMATTED);
        m_marshaller.marshal(m_resultVerification, listModel);
        assertEquals("Marshalled xml does not match the expected output",
                "<DummyListModel><List>"+expected1 + expected2 + expected3+"</List></DummyListModel>",
                m_resultVerification.toString());
    }


    /**
     * Makes sure that the attribute order is in aplhabetical order after marshall. 
     */
    public void testAttributeOrder()
    {
        verifyDummy(new Dummy(true,"I am a dummy", 1000, 14, 10),
                "<Dummy varA=\"true\" varB=\"1000\" varC=\"I am a dummy\" varD=\"14\" varE=\"10\"/>");

        verifyDummy(new Dummy(false,"I am a dummy", 1000, 14, 10),
                "<Dummy varB=\"1000\" varC=\"I am a dummy\" varD=\"14\" varE=\"10\"/>");
    }

    public void testMarshalEnumList()
    {
        ClassModelManager<TestModel> cmm = new ClassModelManager<TestModel>(TestModel.class);
        Unmarshaller<TestModel> u = cmm.getRootUnmarshaller();
        TestModel t = u.unmarshalString("<TestModel enums=\"one,two,four\"/>").getInstance();
        assertEquals(3, t.getEnums().size());
        assertEquals(TestEnum.one, t.getEnums().get(0));
        assertEquals(TestEnum.two, t.getEnums().get(1));
        assertEquals(TestEnum.four, t.getEnums().get(2));
        Marshaller<TestModel> m = cmm.getRootMarshaller();
        String s = m.toXMLString(t);
        assertEquals("<TestModel enums=\"one,two,four,\"/>\n", s);
    }

    /**
     * Verifies that the outcome from the marshalling of 'dummy' is the same as 'expected'.
     * @param dummy Class to marshall
     * @param expected Expected output
     */
    private void verifyDummy(Dummy dummy, String expected)
    {
        m_resultVerification = new StringWriter();
        m_marshaller.marshal(m_resultVerification, dummy);
        assertEquals(expected, m_resultVerification.toString());
    }
    
}
