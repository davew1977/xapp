package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.api.StandaloneNodeUpdate;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.types.ObjLoc;
import net.sf.xapp.objserver.types.PropChange;
import net.sf.xapp.objserver.types.PropChangeSet;
import net.sf.xapp.objserver.types.XmlObj;
import net.sf.xapp.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class IncomingChangesAdaptor implements ObjListener {
    private NodeUpdateApi nodeUpdateApi;
    private ClassDatabase cdb;

    public IncomingChangesAdaptor(ApplicationContainer appContainer) {
        this.nodeUpdateApi = new StandaloneNodeUpdate(appContainer);
        this.cdb = appContainer.getClassDatabase();
    }

    @Override
    public void propertiesChanged(List<PropChangeSet> changeSets) {
        for (PropChangeSet changeSet : changeSets) {
            Long objId = changeSet.getObjId();
            ObjectMeta objectMeta = cdb.findObjById(objId);
            List<PropertyUpdate> updates = new ArrayList<PropertyUpdate>();
            for (PropChange propChange : changeSet.getChanges()) {
                Property property = objectMeta.getProperty(propChange.getProperty());
                Object oldVal = property.convert(objectMeta, propChange.getOldValue());
                Object newVal = property.convert(objectMeta, propChange.getNewValue());
                updates.add(new PropertyUpdate(property, oldVal, newVal));
            }
            nodeUpdateApi.updateObject(objectMeta, updates);
        }
    }

    @Override
    public void objAdded(ObjLoc objLoc, XmlObj obj) {
        Class type = ReflectionUtils.classForName(obj.getType());
    }

    @Override
    public void objMoved(Long id, ObjLoc newObjLoc) {

    }

    @Override
    public void objDeleted(Long id) {

    }
}
