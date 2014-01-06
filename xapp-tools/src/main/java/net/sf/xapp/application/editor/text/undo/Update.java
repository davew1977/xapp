/*
 *
 * Date: 2009-nov-30
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;


import net.sf.xapp.application.editor.text.TextEditor;

public abstract class Update implements Undoable, Cloneable
{
    public int m_offs;
    public String m_text;

    public Update(int offs, String text)
    {
        m_offs = offs;
        m_text = text;
    }

    public abstract boolean followsOnFrom(Update newUpdate);

    protected int length()
    {
        return m_text.length();
    }

    public void merge(Update update)
    {
        assert update.followsOnFrom(this);
        m_text +=update.m_text;
    }

    public Update clone()
    {
        try
        {
            return (Update) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String toString()
    {
        return m_text + "(" + m_offs + ")";
    }

    public abstract void undo(TextEditor textEditor);
    public abstract void redo(TextEditor textEditor);

    void insertInto(TextEditor textEditor)
    {
        textEditor.insert(m_offs, m_text);
    }

    void removeFrom(TextEditor textEditor)
    {
        textEditor.remove(m_offs, length());
    }
}
