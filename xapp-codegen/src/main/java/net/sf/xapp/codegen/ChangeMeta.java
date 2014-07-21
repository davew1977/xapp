/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.codegen.model.Artifact;

import java.util.Set;

public class ChangeMeta
{
    private Set<Artifact> addedItems;
    private Set<Artifact> removedItems;
    private boolean apiOrMessageRenamed;

    public ChangeMeta()
    {
        this(null,null, false);
    }

    public ChangeMeta(Set<Artifact> addedItems, Set<Artifact> removedItems, boolean apiOrMessageRenamed)
    {
        this.addedItems = addedItems;
        this.removedItems = removedItems;
        this.apiOrMessageRenamed = apiOrMessageRenamed;
    }


    public boolean anyAdded()
    {
        return addedItems==null || !addedItems.isEmpty();
    }

    public boolean anyRemoved()
    {
        return addedItems==null || !removedItems.isEmpty();
    }

    public boolean regenApiAndMessageEnums()
    {
        return apiOrMessageRenamed || anyAdded() || anyRemoved();
    }
}
