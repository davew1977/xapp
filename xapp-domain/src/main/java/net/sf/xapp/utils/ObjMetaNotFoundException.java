package net.sf.xapp.utils;

/**
 *
 */
public class ObjMetaNotFoundException extends XappException {
    private Long objId;

    public ObjMetaNotFoundException(Long objId) {
        this.objId = objId;
    }

    public Long getObjId() {
        return objId;
    }
}
