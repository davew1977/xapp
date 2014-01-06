/*
 *
 * Date: 2009-dec-23
 * Author: davidw
 *
 */
package net.sf.xapp.application.editor.widgets;

import java.util.List;

public class SimpleComboChooserClient<T> implements ComboChooserClient<T>
{
    public void itemChosen(T item)
    {

    }

    public List<T> filterValues(String updatedText)
    {
        return null;
    }

    public void selectionChanged(T item)
    {

    }

    public void comboRemoved()
    {

    }

    public boolean isEditable()
    {
        return true;
    }
}