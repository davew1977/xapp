package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.types.ObjPropChange;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.Revision;

/**
* Created by oldDave on 01/12/2014.
*/
public class PotentialPropConflict {
    private final Revision revision;
    private final PropChange serverChange;
    private final ObjectMeta objectMeta;

    public PotentialPropConflict(Revision revision, PropChange serverChange, ObjectMeta objectMeta) {
        this.revision = revision;
        this.serverChange = serverChange;
        this.objectMeta = objectMeta;
    }

    public Revision getRevision() {
        return revision;
    }

    public PropChange getServerChange() {
        return serverChange;
    }

    public ObjectMeta getObjectMeta() {
        return objectMeta;
    }
}
