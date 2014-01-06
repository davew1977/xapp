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

import javax.swing.*;
import java.awt.*;

/**
 * Date: Dec 9, 2008
 * Time: 9:48:46 AM
 */
public class ExceptionPrinter
{
	private static Component m_alertParent;
	public static void initiate(Component alertParent)
	{
		m_alertParent = alertParent;
	}

	/**
	 * Shows an exception to the user
	 *
	 * @param e The exception to be shown
	 * @param overridingMessage An overriding message. This will be printed instead of e.getMessage() in the dialog
	 * 							if it's not null or "".
	 * @param showAlert Whether to show this message in an alert dialog or only in the console.
	 */
	public static void showException(Exception e, String overridingMessage, boolean showAlert)
	{
		String message = "";
		if (overridingMessage == null || overridingMessage.equals(""))
		{
			message = e.getMessage();
		}
		else
		{
			message = overridingMessage;
			System.out.println(overridingMessage);
		}

		if(showAlert && m_alertParent != null)
		{
			JOptionPane.showMessageDialog(m_alertParent, message, "An exception occurred", JOptionPane.ERROR_MESSAGE);
		}


		e.printStackTrace();
	}

	/**
	 * Shows a message to the user
	 *
	 * @param message The message to show
	 * @param showAlert Whether to show the message in an alert dialog or only in the console
	 */
	public static void showMessage(String message, boolean showAlert)
	{
		if(showAlert && m_alertParent != null)
		{
			JOptionPane.showMessageDialog(m_alertParent, message, "Alert", JOptionPane.ERROR_MESSAGE);
		}
		System.out.println(message);
	}


}
