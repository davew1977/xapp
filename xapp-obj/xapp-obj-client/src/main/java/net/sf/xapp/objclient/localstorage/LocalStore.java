package net.sf.xapp.objclient.localstorage;

import java.util.List;

import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LocalStore extends ObjListenerAdaptor implements SaveStrategy{
    public static final String LOCAL_DIR = System.getProperty("user.home", ".") + "/xapp-cache";
    private final String user;
    private final String appId;
    private final ApplicationContainerImpl appContainer;

    public LocalStore(String user, String appId, String objId, ApplicationContainerImpl appContainer) {
        this.user = user;
        this.appId = appId;
        this.appContainer = appContainer;
    }

    public long getLastKnownRevision() {
        //TODO calc last known revision will be value in ${user.home}/user/app/{object.id}/rev.txt
        //TODO + the number of lines in the appender file
        return 0;
    }

    /**
     * replace all client state with a fresh version of the object
     */
    public void reset(XmlObj obj) {
        //TODO delete rev, xml and appender file for this object
        //TODO save the provided xml and create the rev.txt file

    }

    public Object reconstruct(List<Delta> deltas) {

    }

    @Override
    public void save() {
        //todo save the file to ${user.home}/user/app/{object.id}/{object.id}.xml
        //todo save revision meta to  ${user.home}/user/app/{object.id}/rev.txt
        //todo delete appender file (reset)
    }

    @Override
    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
        //todo append to a file specific to this object
        return null;
    }
}
