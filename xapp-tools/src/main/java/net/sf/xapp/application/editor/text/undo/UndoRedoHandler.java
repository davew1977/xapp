/*
 *
 * Date: 2009-dec-01
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;

public interface UndoRedoHandler
{
    boolean canUndo();
    boolean canRedo();
    public Update pullUndo();
    public Update pullRedo();

    
}
