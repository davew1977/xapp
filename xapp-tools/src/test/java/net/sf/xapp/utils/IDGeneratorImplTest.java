package net.sf.xapp.utils;

import junit.framework.TestCase;

/**
 * Author: marie-sofiek
 * Date: Mar 6, 2009
 * Time: 3:40:02 PM
 */
public class IDGeneratorImplTest extends TestCase
{
	IDGenerator m_generator;

	protected void setUp()
	{
		m_generator = IDGeneratorImpl.getInstance();
	}

	public void testNewKeys()
	{
		testNewKey("1");
		testNewKey("2");
		testNewKey("3");
	}

	private void testNewKey(String sequenceNumber)
	{
		String key = m_generator.getNextKey();
		int keyLength = sequenceNumber.length()+6;
		assertTrue("Expected keylength "+keyLength+" but got key "+key, key.length() == keyLength);
		assertTrue("Expected key to start with "+sequenceNumber+"_ but got key "+key, key.startsWith(sequenceNumber+"_"));
	}

	public void testSetKeys()
	{
		int sequenceStart = 42;
		m_generator.setPreviousKey(sequenceStart+"_52361");
		for(int i = sequenceStart+1; i < sequenceStart+6; i++)
		{
			testNewKey(i+"");
		}
	}



	public void testSettingFaultyKeys()
	{
		testSetFaultyKey("f_4254");
		testSetFaultyKey("424f_44");
		testSetFaultyKey("46_gj");
		testSetFaultyKey("34_jjgls");
		testSetFaultyKey("43_kjlk9");
		testSetFaultyKey("512345_4350g");
		testSetFaultyKey("0_35524");
	}

	public void testSettingCorrectKeys()
	{
		testSetCorrectKey("355_52343", "356");
		testSetCorrectKey("1_52343", "2");
	}

	private void testSetCorrectKey(String key, String expectedSequenceNr)
	{
        IDGenerator m_generator = new IDGeneratorImpl();
		try
		{
			m_generator.setPreviousKey(key);
			String newKey = m_generator.getNextKey();
			assertTrue("Expected key to start with "+expectedSequenceNr+" but got key "+newKey, newKey.startsWith(expectedSequenceNr));
		}
		catch(Exception e)
		{
			fail("Threw exception "+e);
		}
	}


	private void testSetFaultyKey(String key)
	{
		Boolean threwException = false;
		try
		{
			m_generator.setPreviousKey(key);
		}
		catch(Exception e)
		{
			threwException = true;
		}

		if(!threwException) fail("No exception was thrown for faulty key "+key);
	}

}
