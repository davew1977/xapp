/*
 *
 * Date: 2009-nov-30
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BufferedEditorListener implements EditorListener
{
    private Timer m_timer;
    private UpdateListener m_delegate;
    private List<Update> m_updates;

    public BufferedEditorListener(UpdateListener updateListener)
    {
        m_delegate = updateListener;
        m_updates = new ArrayList<Update>();
    }

    public void init()
    {
        m_timer = new Timer(5000, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                flush();
            }
        });
        m_timer.setRepeats(true);
        m_timer.start();
    }

    public void flush()
    {
        //merge down the updates
        if (!m_updates.isEmpty())
        {
            List<Update> merged = new ArrayList<Update>();
            Update newUpdate = m_updates.get(0);
            merged.add(newUpdate);
            for (int i = 1; i < m_updates.size(); i++)
            {
                Update update = m_updates.get(i);
                if (update.followsOnFrom(newUpdate))
                {
                    newUpdate.merge(update);
                }
                else
                {
                    newUpdate = update.clone();
                    merged.add(newUpdate);
                }
            }
            m_updates.clear();
            m_delegate.updates(merged);
        }
    }

    public void clear()
    {
        m_updates.clear();
    }

    public void textAdded(int offs, String newText)
    {
        m_updates.add(new AddUpdate(offs,newText));
    }

    public void textRemoved(int offs, String removedText)
    {
        m_updates.add(new RemoveUpdate(offs, removedText));
    }
}
