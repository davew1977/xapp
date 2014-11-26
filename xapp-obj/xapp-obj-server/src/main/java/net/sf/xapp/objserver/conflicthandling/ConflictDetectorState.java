package net.sf.xapp.objserver.conflicthandling;

import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjLoc;

/**
 */
public abstract class ConflictDetectorState implements ObjUpdate{
    protected final ConflictDetector conflictDetector;

    protected ConflictDetectorState(ConflictDetector conflictDetector) {
        this.conflictDetector = conflictDetector;
    }

    protected IdProp idProp(ObjectLocation objectLocation) {
        return new IdProp(objectLocation.getObj().getId(), objectLocation.getProperty().getName());
    }

    protected ObjectLocation toObjLocation(ObjLoc objLoc) {
        return conflictDetector.rootObj.toObjLocation(objLoc);
    }

    protected Delta current() {
        return conflictDetector.current;
    }
}
