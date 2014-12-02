package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.objserver.types.ObjPropChange;
import net.sf.xapp.objserver.types.Revision;

/**
* Created by oldDave on 01/12/2014.
*/
public class PotentialPropConflict {
    private final Revision revision;
    private final ObjPropChange serverChange;

    public PotentialPropConflict(Revision revision, ObjPropChange serverChange) {
        this.revision = revision;
        this.serverChange = serverChange;
    }

    public Revision getRevision() {
        return revision;
    }

    public ObjPropChange getServerChange() {
        return serverChange;
    }
}
