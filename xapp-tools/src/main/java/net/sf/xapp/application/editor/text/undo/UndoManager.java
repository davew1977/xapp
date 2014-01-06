/*
 *
 * Date: 2009-dec-02
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;

public class UndoManager implements EditorListener, UndoRedoHandler
{
    private EditorListener m_state;
    private BufferedEditorListener m_bufferedEditorListener;
    private NullEditorListener m_nullEditorListener;
    private DefaultUndoRedoHandler m_undoRedoHandler;

    public UndoManager()
    {
        m_nullEditorListener = new NullEditorListener();
        m_undoRedoHandler = new DefaultUndoRedoHandler();
        m_bufferedEditorListener = new BufferedEditorListener(m_undoRedoHandler);
        m_bufferedEditorListener.init();
        m_state = m_nullEditorListener;
    }

    public void enable()
    {
        m_state = m_bufferedEditorListener;
    }

    public void disable()
    {
        m_state = m_nullEditorListener;
    }

    public void textAdded(int offs, String newText)
    {
        m_state.textAdded(offs, newText);
    }

    public void textRemoved(int offs, String removedText)
    {
        m_state.textRemoved(offs, removedText);
    }

    public void init()
    {
        m_bufferedEditorListener.clear();
        m_undoRedoHandler.reset();
    }

    public boolean canUndo()
    {
        return m_undoRedoHandler.canUndo();
    }

    public boolean canRedo()
    {
        return m_undoRedoHandler.canRedo();
    }

    public Update pullUndo()
    {
        return m_undoRedoHandler.pullUndo();
    }

    public Update pullRedo()
    {
        return m_undoRedoHandler.pullRedo();
    }

    public void flush()
    {
        m_bufferedEditorListener.flush();
    }
}
