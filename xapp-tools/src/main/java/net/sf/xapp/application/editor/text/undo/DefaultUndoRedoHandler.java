/*
 *
 * Date: 2009-nov-30
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;

import java.util.ArrayList;
import java.util.List;

public class DefaultUndoRedoHandler implements UpdateListener, UndoRedoHandler
{
    private int m_pointer = 0;
    private List<Update> m_updates;

    public DefaultUndoRedoHandler()
    {
        m_updates = new ArrayList<Update>();
    }

    public void updates(List<Update> merged)
    {
        System.out.println(merged);
        if (canRedo()) //throw away updates after pointer
        {
            m_updates = m_updates.subList(0, m_pointer);
        }
        m_updates.addAll(merged);
        m_pointer = m_updates.size();
    }

    public boolean canUndo()
    {
        return m_pointer > 0;
    }

    public boolean canRedo()
    {
        return m_pointer < m_updates.size();
    }

    public Update pullUndo()
    {
        return m_updates.get(--m_pointer);
    }

    public Update pullRedo()
    {
        return m_updates.get(m_pointer++);
    }

    public void reset()
    {
        m_updates = new ArrayList<Update>();
    }
}
