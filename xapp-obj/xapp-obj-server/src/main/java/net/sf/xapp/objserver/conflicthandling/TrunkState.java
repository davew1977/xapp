package net.sf.xapp.objserver.conflicthandling;

import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;

/**
 */
public class TrunkState extends ConflictDetectorState {

    public TrunkState(ConflictDetector conflictDetector) {
        super(conflictDetector);
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        ObjectLocation objectLocation = toObjLocation(objLoc);
        if(!objectLocation.isCollection()) { //obj creations are ok if the objloc is a collection
            conflictDetector.deltaMap.put(idProp(objectLocation), current());
        }
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {

    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {

    }

    @Override
    public void deleteObject(UserId principal, Long id) {

    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {

    }

    @Override
    public void changeType(UserId principal, Long id, Class newType) {

    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {

    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {

    }
}
