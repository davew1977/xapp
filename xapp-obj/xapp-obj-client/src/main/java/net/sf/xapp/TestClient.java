package net.sf.xapp;

import net.sf.xapp.net.client.framework.ClientContext;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.client.io.ServerProxyImpl;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class TestClient {
    public static void main(String[] args) {

        final ObjClientContext clientContext = new ObjClientContext(args[0], new ServerProxyImpl(HostInfo.parse("11375")));
        clientContext.connect();
        clientContext.login();

        clientContext.wire(ObjManagerReply.class, "s1", new ObjManagerReply() {
            @Override
            public void getObjectResponse(UserId principal, XmlObj obj, ErrorCode errorCode) {
                System.out.println(obj.getData());
            }

            @Override
            public void getDeltasResponse(UserId principal, List<Delta> deltas, ErrorCode errorCode) {

            }
        });

        clientContext.objManager("s1").getObject(clientContext.getUserId());

    }
}
