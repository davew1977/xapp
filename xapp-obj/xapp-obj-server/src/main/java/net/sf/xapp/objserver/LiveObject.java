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
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LiveObject extends SimpleObjUpdater {
    private ObjListener listener;
    private long latestRevision;

    public LiveObject(ObjectMeta rootObject) {
        super(rootObject);
        listener = new ObjListenerAdaptor(null, new Multicaster<ObjListener>());

        addListener(new ObjListenerAdaptor() {
            @Override
            public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
                latestRevision++;
                return null;
            }
        });
    }

    public void addListener(ObjListener li) {
        ((Multicaster<ObjListener>)((ObjListenerAdaptor)listener).getDelegate()).addDelegate(li);
    }

    @Override
    public void createObject(UserId principal, ObjLoc objLoc, Class type, String xml) {
        super.createObject(principal, objLoc, type, xml);
        ObjectMeta objectMeta = cdb.lastCreated();
        listener.objAdded(principal, objLoc, toXmlObj(objectMeta));
    }

    @Override
    public void createEmptyObject(UserId principal, ObjLoc objLoc, Class type) {
        super.createEmptyObject(principal, objLoc, type);
        ObjectMeta objectMeta = cdb.lastCreated();
        listener.objCreated(principal, objLoc, toXmlObj(objectMeta));
    }

    @Override
    public void updateObject(UserId principal, List<PropChangeSet> changeSets) {
        super.updateObject(principal, changeSets);
        listener.propertiesChanged(principal, changeSets);
    }

    @Override
    public void deleteObject(UserId principal, Long id) {
        super.deleteObject(principal, id);
        listener.objDeleted(principal, id);
    }

    @Override
    public void moveObject(UserId principal, Long id, ObjLoc newObjLoc) {
        super.moveObject(principal, id, newObjLoc);
        listener.objMoved(principal, id, newObjLoc);
    }

    @Override
    public void changeType(UserId principal, Long id, Class newType) {
        super.changeType(principal, id, newType);
        ObjectMeta objectMeta = cdb.lastCreated();
        ObjectLocation objHome = objectMeta.getHome();
        int index = objectMeta.index();
        //should not be needed : objHome.setIndex(newInstance, oldIndex);
        listener.typeChanged(principal, new ObjLoc(objHome.getObj().getId(), objHome.getProperty().getName(), index), id, toXmlObj(objectMeta));
    }

    @Override
    public void moveInList(UserId principal, ObjLoc objLoc, Long id, Integer delta) {
        super.moveInList(principal, objLoc, id, delta);
        listener.objMovedInList(principal, objLoc, id, delta);
    }

    @Override
    public void updateRefs(UserId principal, ObjLoc objLoc, List<Long> refsToAdd, List<Long> refsToRemove) {
        super.updateRefs(principal, objLoc, refsToAdd, refsToRemove);

        listener.refsUpdated(principal, objLoc, refsToAdd, refsToRemove);
    }

    public long getLatestRevision() {
        return latestRevision;
    }

    public XmlObj createXmlObj() {
        return new XmlObj(rootObj.getType(), rootObj.toXml(), latestRevision, rootObj.getId());
    }
}
