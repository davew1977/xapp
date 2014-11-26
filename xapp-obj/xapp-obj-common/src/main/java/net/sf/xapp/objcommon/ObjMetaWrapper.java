package net.sf.xapp.objcommon;

import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.types.ObjLoc;

/**
 *
 */
public class ObjMetaWrapper {
    private ObjectMeta objMeta;

    public ObjMetaWrapper(ObjectMeta objMeta) {
        this.objMeta = objMeta;
    }

    public ObjectMeta getObjMeta() {
        return objMeta;
    }

    public ClassDatabase cdb() {
        return objMeta.getClassDatabase();
    }

    public Class getType() {
        return objMeta.getType();
    }

    public ObjectLocation toObjLocation(ObjLoc objLoc) {
        ObjectMeta objectMeta = cdb().findObjById(objLoc.getId());
        return new ObjectLocation(objectMeta, objectMeta.getProperty(objLoc.getProperty()));
    }
}
