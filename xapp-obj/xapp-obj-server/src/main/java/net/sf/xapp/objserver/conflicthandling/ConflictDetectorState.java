package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjInfo;
import net.sf.xapp.objserver.types.ObjLoc;

/**
 */
public abstract class ConflictDetectorState{
    protected final ConflictDetector conflictDetector;

    protected ConflictDetectorState(ConflictDetector conflictDetector) {
        this.conflictDetector = conflictDetector;
    }

    protected IdProp toIdProp(ObjLoc objLoc) {
        return new IdProp(objLoc.getId(), objLoc.getProperty());
    }

    protected ObjectLocation toObjLocation(ObjLoc objLoc) {
        return conflictDetector.liveObject.getRootObj().toObjLocation(objLoc);
    }


    public static ObjInfo createObjInfo(ObjectMeta objectMeta) {
        return new ObjInfo(objectMeta.getId(), objectMeta.getGlobalKey(), objectMeta.getType());
    }
}
