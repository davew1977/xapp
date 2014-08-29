package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class IncomingChangesAdaptor implements ObjListener {
    private ApplicationContainer appContainer;
    @Override
    public void propertiesChanged(List<PropChangeSet> changeSets) {

    }

    @Override
    public void objAdded(ObjLoc objLoc, XmlObj obj) {

    }

    @Override
    public void objMoved(Long id, ObjLoc newObjLoc) {

    }

    @Override
    public void objDeleted(Long id) {

    }
}
