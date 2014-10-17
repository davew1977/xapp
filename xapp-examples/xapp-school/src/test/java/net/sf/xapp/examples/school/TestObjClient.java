package net.sf.xapp.examples.school;

import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.objclient.ObjClient;

/**
 * © 2014 Webatron Ltd
 * Created by dwebber
 */
public class TestObjClient extends ObjClient {

    public TestObjClient(String localDir, String userId, String appId, String objId) {
        super(localDir, userId, HostInfo.parse("11375"), appId, objId);
    }

    @Override
    protected void objMetaLoaded() {

    }
}
