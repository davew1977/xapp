package net.sf.xapp.objserver;

import java.util.List;

import net.sf.xapp.objserver.types.Delta;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface ObjVersionManager {

    /**
     *
     * get all deltas from a given rev to head
     */
    List<Delta> getDeltas(long fromRev);

    /**
     * get the latest version of the object
     */
    Object getHead();
}
