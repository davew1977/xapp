package net.sf.xapp.uifwk;

import java.util.ArrayList;

public class SelectionGroup extends ArrayList<Selectable>
{
    public void setSelected(Selectable selectable, boolean selected)
    {
        for (Selectable sel : this)
        {
            sel.setSelected(false);
        }
        selectable.setSelected(selected);
    }
}
