package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.objserver.types.ObjInfo;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.Revision;

/**
* Created by oldDave on 01/12/2014.
*/
public class PotentialPropConflict {
    private final Revision revision;
    private final PropChange serverChange;
    private final ObjInfo objInfo;

    public PotentialPropConflict(Revision revision, PropChange serverChange, ObjInfo objInfo) {
        this.revision = revision;
        this.serverChange = serverChange;
        this.objInfo = objInfo;
    }

    public Revision getRevision() {
        return revision;
    }

    public PropChange getServerChange() {
        return serverChange;
    }

    public ObjInfo getObjInfo() {
        return objInfo;
    }

}
