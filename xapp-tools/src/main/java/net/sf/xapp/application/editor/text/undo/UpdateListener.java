/*
 *
 * Date: 2009-dec-02
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.text.undo;

import java.util.List;

public interface UpdateListener
{
    void updates(List<Update> updates);      
}
