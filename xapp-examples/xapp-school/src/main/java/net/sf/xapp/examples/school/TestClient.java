package net.sf.xapp.examples.school;

import net.sf.xapp.objclient.IncomingChangesAdaptor;
import net.sf.xapp.objclient.NodeUpdateApiRemote;
import net.sf.xapp.objclient.ObjClientContext;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.examples.school.model.SchoolSystem;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.client.io.HostInfo;
import net.sf.xapp.net.client.io.ServerProxyImpl;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerReply;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;

import java.io.File;
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


        final String remoteKey = "s1";
        clientContext.wire(ObjManagerReply.class, remoteKey, new ObjManagerReply() {
            @Override
            public void getObjectResponse(UserId principal, XmlObj obj, ErrorCode errorCode) {
                Unmarshaller unmarshaller = new Unmarshaller(SchoolSystem.class);
                ObjectMeta objMeta = unmarshaller.unmarshalString(obj.getData());
                ClassDatabase cdb = unmarshaller.getClassDatabase();
                ApplicationContainerImpl appContainer = new ApplicationContainerImpl(new DefaultGUIContext(new File("file.xml"), cdb, objMeta));
                appContainer.setUserGUI(new SimpleApplication());
                appContainer.getMainFrame().setVisible(true);

                appContainer.setNodeUpdateApi(new NodeUpdateApiRemote(appContainer, clientContext.remoteObjListener(remoteKey)));
                clientContext.wire(ObjListener.class, remoteKey, new IncomingChangesAdaptor(appContainer));
            }

            @Override
            public void getDeltasResponse(UserId principal, List<Delta> deltas, ErrorCode errorCode) {

            }
        });


        clientContext.objManager(remoteKey).getObject(clientContext.getUserId());

    }
}
