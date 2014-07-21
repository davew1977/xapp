package net.sf.xapp.net.common.framework;

import net.sf.xapp.annotations.objectmodelling.Key;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface Entity extends TransportObject{

    @Key
    String getKey();
    void init();
}
