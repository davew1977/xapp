package net.sf.xapp.objserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objcommon.ObjMetaWrapper;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.IdProp;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;

/**
 */
public class ConflictDetector implements ObjUpdate {

    private ObjMetaWrapper rootObj;
    private boolean processingTrunk;
    private Set<Long> deletedObjects = new HashSet<>();
    private Set<Long> phantomObjects = new HashSet<>(); //these objects could not be created by the new delta stream - negative numbers
    private Map<IdProp, Delta> deltaMap = new HashMap<>();

    private Delta current;

    public ConflictDetector(ObjMetaWrapper rootObj) {
        this.rootObj = rootObj;
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        ObjectLocation objectLocation = rootObj.toObjLocation();
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        //can't cause conflict
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

    public void process(List<Delta> trunkDeltas, List<Delta> branchDeltas) {
        processingTrunk = true;
        for (Delta trunkDelta : trunkDeltas) {
            current = trunkDelta;
            trunkDelta.getMessage().visit(this);
        }
        processingTrunk = false;
        for (Delta branchDelta : branchDeltas) {
            current = branchDelta;
            branchDelta.getMessage().visit(this);
        }
    }
}
