package net.sf.xapp.objcommon;

import net.sf.xapp.net.common.framework.Multicaster;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.util.List;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class LiveObject extends SimpleObjUpdater {
    private ObjListener listener;

    public LiveObject(ObjectMeta rootObject) {
        super(rootObject, true);
        listener = new ObjListenerAdaptor(null, new Multicaster<ObjListener>());
    }

    @Override
    protected void objAdded(UserId principal, ObjLoc objLoc, ObjectMeta objectMeta) {
        listener.objAdded(principal, cdb().getRev(), objLoc, toXmlObj(objectMeta));
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        super.updateObject(principal, changeSets);
        listener.propertiesChanged(principal, cdb().getRev(), changeSets);
    }

    @Override
    public void deleteObject(UserId principal, ObjLoc oldLoc, Long id) {
        super.deleteObject(principal, oldLoc, id);
        listener.objDeleted(principal, cdb().getRev(), oldLoc, id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc oldLoc, ObjLoc newObjLoc) {
        super.moveObject(principal, id, oldLoc, newObjLoc);
        listener.objMoved(principal, cdb().getRev(), id, oldLoc, newObjLoc);
    }

    @Override
    protected void typeChanged(UserId principal, ObjLoc objLoc, Long oldId, ObjectMeta newInstance) {
        //should not be needed : objHome.setIndex(newInstance, oldIndex);
        listener.objDeleted(principal, cdb().getRev(), objLoc, oldId);
        listener.objAdded(principal, cdb().getRev(), objLoc, toXmlObj(newInstance));
    }

    @Override
    public void setIndex(UserId principal, ObjLoc objLoc, Long id, Integer newIndex) {
        super.setIndex(principal, objLoc, id, newIndex);
        listener.objIndexChanged(principal, cdb().getRev(), objLoc, id, newIndex);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        super.updateRefs(principal, objLoc, refsToAdd, refsToRemove);

        listener.refsUpdated(principal, cdb().getRev(), objLoc, refsToAdd, refsToRemove);
    }

    public XmlObj createXmlObj() {
        return new XmlObj(rootObj.getType(), rootObj.toXml(), cdb().getRev(), rootObj.getId());
    }

    public void addListener(ObjListener li) {
        ((Multicaster<ObjListener>)((ObjListenerAdaptor)listener).getDelegate()).addDelegate(li);
    }
}
