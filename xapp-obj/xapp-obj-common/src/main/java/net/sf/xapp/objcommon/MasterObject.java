package net.sf.xapp.objcommon;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;

/**
 * Created by oldDave on 26/12/2014.
 */
public class MasterObject extends LiveObject {
    public MasterObject(ObjectMeta rootObject) {
        super(rootObject);
    }

    @Override
    public <T> T handleMessage(InMessage<ObjUpdate, T> inMessage) {
        rootObj.getObjMeta().incrementRev();
        return super.handleMessage(inMessage);
    }
}
