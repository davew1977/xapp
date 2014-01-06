/*
 *
 * Date: 2009-dec-02
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;


import net.sf.xapp.application.editor.text.TextEditor;

public class RemoveUpdate extends Update
{
    public RemoveUpdate(int offs, String text)
    {
        super(offs, text);
    }

    public boolean followsOnFrom(Update newUpdate)
    {
        return newUpdate instanceof RemoveUpdate && m_offs == newUpdate.m_offs - length();
    }

    public void merge(Update update)
    {
        assert update.followsOnFrom(this);
        m_text = update.m_text + m_text;
        m_offs = update.m_offs;
    }

    public String toString()
    {
        return "-" + super.toString();
    }

    public void undo(TextEditor textEditor)
    {
        insertInto(textEditor);
    }

    public void redo(TextEditor textEditor)
    {
        removeFrom(textEditor);
    }
}
