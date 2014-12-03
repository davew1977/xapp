package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjInfo;
import net.sf.xapp.objserver.types.ObjLoc;

/**
 */
public abstract class ConflictDetectorState implements ObjUpdate{
    protected final ConflictDetector conflictDetector;

    protected ConflictDetectorState(ConflictDetector conflictDetector) {
        this.conflictDetector = conflictDetector;
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        createObj(objLoc);
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        createObj(objLoc);
    }

    protected abstract void createObj(ObjLoc objLoc);

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
