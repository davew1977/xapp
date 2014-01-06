/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.utils;

/**
 * Date: Mar 6, 2009
 * Time: 2:30:13 PM
 */
public class IDGeneratorImpl implements IDGenerator
{
	private int m_previousKeyNumber = -1;
	private static IDGenerator m_instance;

	private final String CATCHPHRASE = " Key should be on the form X_YYYYY where X is a sequence number of any size (minimum 1), and Y are five random digits.";

	public IDGeneratorImpl()
	{}

	public static IDGenerator getInstance()
	{
		if(m_instance==null)
		{
			m_instance = new IDGeneratorImpl();
		}

		return m_instance;
	}

	/**
	 *
	 * @param key Key should be on the form X_YYYYY where X is a positive sequence number of any size (minimum 1), and Y are five random digits.
	 */
	public void setPreviousKey(String key)
	{
		String sequenceNumber = "";
		char[] keyChars = key.toCharArray();
		//if the first character is not a positive digit, something's wrong
		if(keyChars.length<1 || !Character.isDigit(keyChars[0]) || keyChars[0]=='0')
		{
			throw new RuntimeException("Invalid start of key "+key+"."
					+ CATCHPHRASE);
		}
		for(char c : keyChars)
		{
			if(Character.isDigit(c))
			{
				sequenceNumber+=c;
			}
			else if(c == '_')
			{
				char[] randoms = key.substring(key.indexOf('_')+1).toCharArray();
				if(randoms.length!=5)
				{
					throw new RuntimeException("Expected five random digits after the underscore ('_') of key "+key+" but found "+randoms.length+" characters. "
						+ CATCHPHRASE);
				}


				//loop through all numbers after '_' and make sure they're numeric, otherwise we haven't passed
				for(char randomNumber : randoms)
				{
					if(!Character.isDigit(randomNumber))
					{
						throw new RuntimeException("Expected five random digits after the underscore ('_') of key "+key+" but ran into character "+randomNumber+". "
								+ CATCHPHRASE);
												}
				}

				break;
			}
			else
			{
				throw new RuntimeException("Expected underscore ('_') after the first digits of key "+key+". "
						+ CATCHPHRASE);
								}
		}

		m_previousKeyNumber = Integer.parseInt(sequenceNumber);
	}

	public String getNextKey()
	{
		if (m_previousKeyNumber == -1)
		{
			System.out.println("Previous key number has not been set. Generating keys starting with 1.");
			m_previousKeyNumber = 0;
		}

		m_previousKeyNumber++;

		String key = m_previousKeyNumber + "_";
		for(int i=0; i < 5; i++)
		{
			key += getRandomNumber();
		}

		return key;
	}

	private int getRandomNumber()
	{
		return (int) (Math.random()*10);
	}

}
