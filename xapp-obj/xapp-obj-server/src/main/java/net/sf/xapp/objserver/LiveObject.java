package net.sf.xapp.objserver;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.framework.Adaptor;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.Multicaster;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objcommon.SimpleObjUpdater;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
        listener.objAdded(principal, cdb.getRev(), objLoc, toXmlObj(objectMeta));
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        super.updateObject(principal, changeSets);
        listener.propertiesChanged(principal, cdb.getRev(), changeSets);
    }

    @Override
    public void deleteObject(UserId principal, Long id) {
        super.deleteObject(principal, id);
        listener.objDeleted(principal, cdb.getRev(), id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {
        super.moveObject(principal, id, newObjLoc);
        listener.objMoved(principal, cdb.getRev(), id, newObjLoc);
    }

    @Override
    protected void typeChanged(UserId principal, Long oldId, ObjectMeta newInstance) {
        ObjectLocation objHome = newInstance.getHome();
        int index = newInstance.index();
        //should not be needed : objHome.setIndex(newInstance, oldIndex);
        listener.typeChanged(principal, cdb.getRev(), new ObjLoc(objHome.getObj().getId(), objHome.getProperty().getName(), index), oldId, toXmlObj(newInstance));
    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        super.moveInList(principal, objLoc, id, delta);
        listener.objMovedInList(principal, cdb.getRev(), objLoc, id, delta);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        super.updateRefs(principal, objLoc, refsToAdd, refsToRemove);

        listener.refsUpdated(principal, cdb.getRev(), objLoc, refsToAdd, refsToRemove);
    }

    public XmlObj createXmlObj() {
        return new XmlObj(rootObj.getType(), rootObj.toXml(), cdb.getRev(), rootObj.getId());
    }

    public void addListener(ObjListener li) {
        ((Multicaster<ObjListener>)((ObjListenerAdaptor)listener).getDelegate()).addDelegate(li);
    }
}
