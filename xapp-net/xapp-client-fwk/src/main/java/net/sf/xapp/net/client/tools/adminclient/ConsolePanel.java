package net.sf.xapp.net.client.tools.adminclient;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class ConsolePanel extends JTextArea
{
    private int m_maxLines;
    private int m_noLines;

    public ConsolePanel(int maxLines)
    {
        m_maxLines = maxLines;
        Font f = new Font("Lucida Console", Font.PLAIN, 12);
        Color textCol = Color.decode("#CCCCCC");
        Color bgCol = Color.decode("#000000");
        setFont(f);
        setBackground(bgCol);
        setForeground(textCol);
        setAutoscrolls(true);
        setEditable(false);
    }

    public synchronized void clear()
    {
        setText("");
        m_noLines = 0;
    }

    @Override
    public synchronized void append(String str)
    {
        super.append(str);
        boolean newline = str.indexOf('\n') != -1;
        if(newline)
        {
            m_noLines++;
        }
        if(newline && m_noLines > m_maxLines)
        {
            try
            {
                getDocument().remove(0, getText().indexOf('\n') + 1);
            }
            catch (BadLocationException e)
            {
                throw new RuntimeException(e);
            }
            m_noLines--;
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Rectangle b = getBounds();
                scrollRectToVisible(new Rectangle(0,b.height-10, b.width, 10));
            }
        });
    }
}