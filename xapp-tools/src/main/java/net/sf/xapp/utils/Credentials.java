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

import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Credentials extends JDialog
{
    private boolean m_ok = false;
    private JTextField m_usernameTF;
    private JPasswordField m_passwordTF;

    public Credentials(Frame owner, boolean modal, String defaultUsername, String defaultPassword)
    {
        super(owner, modal);
        JLabel usernameLabel = new JLabel("username");
        JLabel passwordLabel = new JLabel("password");
        usernameLabel.setPreferredSize(new Dimension(65,20));
        passwordLabel.setPreferredSize(new Dimension(65,20));

        Box main = Box.createVerticalBox();
        Box row1 = Box.createHorizontalBox();
        Box row2 = Box.createHorizontalBox();
        Box row3 = Box.createHorizontalBox();
        m_usernameTF = new JTextField(defaultUsername);
        m_usernameTF.setPreferredSize(new Dimension(100,20));
        m_passwordTF = new JPasswordField(defaultPassword);
        m_passwordTF.setPreferredSize(new Dimension(100,20));
        m_passwordTF.setAction(new OKAction());
        JButton ok = new JButton("ok");
        ok.setPreferredSize(new Dimension(70,20));
        ok.addActionListener(new OKAction());
        JButton cancel = new JButton("cancel");
        cancel.setPreferredSize(new Dimension(70,20));
        cancel.addActionListener(new CancelAction());
        row1.add(Box.createHorizontalStrut(10));
        row1.add(usernameLabel);
        row1.add(m_usernameTF);
        row1.add(Box.createHorizontalStrut(10));
        row2.add(Box.createHorizontalStrut(10));
        row2.add(passwordLabel);
        row2.add(m_passwordTF);
        row2.add(Box.createHorizontalStrut(10));
        row3.add(ok);
        row3.add(cancel);
        main.add(Box.createVerticalStrut(4));
        main.add(row1);
        main.add(row2);
        main.add(Box.createVerticalStrut(10));
        main.add(row3);
        setContentPane(main);
        pack();
        SwingUtils.setFont(this, SwingUtils.DEFAULT_FONT);
        if(owner==null)
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((int)screenSize.getWidth() / 2 - getWidth() / 2, (int) screenSize.getHeight() / 2 - getHeight() / 2 );

        }
        setTitle("Login");
        if (SwingUtils.DEFAULT_FRAME_ICON!=null)
        {

            //setIconImage(SwingUtils.DEFAULT_FRAME_ICON.getImage());
        }
    }



	public static String[] obtainCredentials(Frame owner, String defaultUsername, String defaultPassword)
	{
		return obtainCredentials(owner, defaultUsername, defaultPassword, false);
	}

	public static String[] obtainCredentials(Frame owner, String defaultUsername, String defaultPassword, boolean forceInput)
    {
        while(true)
		{
			Credentials credentials= new Credentials(owner, true, defaultUsername, defaultPassword);
			credentials.setVisible(true);
			credentials.dispose();

			String[] creds = new String[]{credentials.m_usernameTF.getText(),
							String.valueOf(credentials.m_passwordTF.getPassword())};

			if(forceInput
					&& (creds[0] == null || creds[0].equals("")) //We only REALLY care about username since that is shown in the client
					&& credentials.m_ok)
			{
				JOptionPane.showMessageDialog(owner, "You have to enter credentials to log in!");
			}
			else
			{
				return credentials.m_ok ?
					creds :
					null;
			}
		}
	}

    public static void main(String[] args) throws NoSuchFieldException
    {
        String[] creds = obtainCredentials(null, "boo", "bar");
        System.out.println(creds[0] + " " + creds[1]);


    }

    private class OKAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            m_ok = true;
            setVisible(false);
        }
    }

    private class CancelAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            setVisible(false);
        }
    }
}
