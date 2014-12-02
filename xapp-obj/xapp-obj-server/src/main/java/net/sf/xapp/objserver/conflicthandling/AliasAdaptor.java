package net.sf.xapp.objserver.conflicthandling;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objcommon.LiveObject;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;

/**
 * A specialized adaptor for applying local changes to the live object, local object ids have to be converted to global ones
 */
public class AliasAdaptor implements ObjUpdate {
    private final LiveObject liveObject;
    private long offset;

    public AliasAdaptor(LiveObject liveObject, long clientIdStart) {
        this.liveObject = liveObject;
        long nextId = liveObject.cdb().peekNextId();
        offset = nextId - clientIdStart;
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        liveObject.createEmptyObject(principal, objLoc, type);
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        liveObject.createObject(principal, objLoc, type, xml);
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        List<PropChangeSet> alteredChangeSets = new ArrayList<>();
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            PropChangeSet propChangeSet = new PropChangeSet(fixId(objId), changeSet.getChanges());
            alteredChangeSets.add(propChangeSet);
        }
        liveObject.updateObject(principal, alteredChangeSets);
    }

    @Override
    public void deleteObject(UserId principal, ObjLoc objLoc, Long id) {
        liveObject.deleteObject(principal, fix(objLoc), fixId(id));
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldObjLoc, ObjLoc newObjLoc) {
        liveObject.moveObject(principal, fixId(id), fix(oldObjLoc), fix(newObjLoc));
    }

    @Override
    public void changeType(UserId principal, ObjLoc objLoc, Long id, Class newType) {
        liveObject.changeType(principal, fix(objLoc), fixId(id), newType);
    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        liveObject.setIndex(principal, fix(objLoc), fixId(id), delta);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        liveObject.updateRefs(principal, fix(objLoc), fix(refsToAdd), fix(refsToRemove));
    }

    private Long fixId(Long objId) {
        return objId < 0 ? objId + offset : objId;
    }

    private ObjLoc fix(ObjLoc objLoc) {
        return new ObjLoc(fixId(objLoc.getId()), objLoc.getProperty(), objLoc.getIndex());
    }

    private List<Long> fix(List<Long> objIds) {
        List<Long> result = new ArrayList<>();
        for (Long objId : objIds) {
            result.add(fixId(objId));
        }
        return result;
    }
}
