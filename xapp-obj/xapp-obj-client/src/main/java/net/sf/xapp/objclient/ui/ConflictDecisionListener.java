package net.sf.xapp.objclient.ui;

import net.sf.xapp.objserver.types.ConflictResolution;

/**
 * Created by oldDave on 13/12/2014.
 */
public interface ConflictDecisionListener {
    public void decision(ConflictResolution decision);
}
