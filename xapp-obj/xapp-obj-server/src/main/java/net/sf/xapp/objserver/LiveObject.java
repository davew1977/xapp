package net.sf.xapp.objserver;

import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LiveObject implements ObjUpdate {
    private ObjListener listener;

    @Override
    public void createObject(ObjLoc objLoc, Class type, String xml) {

        Long rev = 0L;
        Long id = 0L;
        listener.objAdded(objLoc, new XmlObj(type, xml, rev, id));
    }

    @Override
    public void updateObject(List<PropChangeSet> changeSets) {

        listener.propertiesChanged(changeSets);
    }

    @Override
    public void deleteObject(Long id) {

        listener.objDeleted(id);
    }

    @Override
    public void moveObject(Long id, ObjLoc newObjLoc) {

        listener.objMoved(id, newObjLoc);
    }

    @Override
    public void changeType(Long id, Class newType) {

        listener.typeChanged(id, newType);
    }

    @Override
    public void moveInList(ObjLoc objLoc, Long id, Integer delta) {

        listener.objMovedInList(objLoc, id, delta);
    }

    @Override
    public void updateRefs(ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        listener.refsUpdated(objLoc, refsToAdd, refsToRemove);
    }
}
