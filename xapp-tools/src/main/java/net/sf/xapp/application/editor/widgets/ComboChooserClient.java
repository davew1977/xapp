/*
 *
 * Date: 2009-dec-20
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.widgets;

import java.util.List;

public interface ComboChooserClient<T>
{
    void itemChosen(T item);

    /**
     *
     * @param updatedText
     * @return null if filtering is not required
     */
    List<T> filterValues(String updatedText);

    void selectionChanged(T item);

    void comboRemoved();

    boolean isEditable();
}