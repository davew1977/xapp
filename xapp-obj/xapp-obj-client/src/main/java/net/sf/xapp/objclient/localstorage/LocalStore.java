package net.sf.xapp.objclient.localstorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.objcommon.SimpleObjUpdater;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objlistener.ObjListener;
import net.sf.xapp.objserver.apis.objlistener.ObjListenerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;
import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.ant.AntFacade;

import org.apache.tools.ant.util.LineOrientedOutputStream;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LocalStore extends ObjUpdateAdaptor implements SaveStrategy{
    public static final String LOCAL_DIR = System.getProperty("user.home", ".") + "/xapp-cache";
    private final File dir;
    private final File revFile;
    private final File objFile;
    private final File deltaFile;
    private final String[] initialDeltas;
    private OutputStreamWriter deltaWriter;
    private final String user;
    private final String appId;
    private final String objId;
    
    private ObjectMeta objMeta;
    private ClassDatabase cdb;

    public LocalStore(String user, String appId, String objId) {
        this.user = user;
        this.appId = appId;
        this.objId = objId;
        dir = new File(new File(new File(LOCAL_DIR, user), appId), objId);
        dir.mkdirs();
        revFile = new File(dir, "rev.txt");
        objFile = new File(dir, "obj.xml");
        deltaFile = new File(dir, "deltas.txt");
        initialDeltas = FileUtils.readFile(deltaFile, Charset.forName("UTF-8")).split("\n");
    }

    public long getLastKnownRevision() {
        if(revFile.exists()) {
            long rev = Long.parseLong(FileUtils.readFile(revFile));
            return rev + initialDeltas.length;
        }
        return -1;
    }

    /**
     * replace all client state with a fresh version of the object
     */
    public void reset(XmlObj obj) {
        closeDeltaWriter();
        new AntFacade().deleteFile(deltaFile);
        FileUtils.writeFile(obj.getData(), objFile);
        FileUtils.writeFile(obj.getLastChangeRev(), objFile);
        Unmarshaller unmarshaller = new Unmarshaller(obj.getType());
        objMeta = unmarshaller.unmarshalString(obj.getData());
        cdb = unmarshaller.getClassDatabase();
    }

    public Object reconstruct(Class type, List<Delta> deltas) {
        Unmarshaller unmarshaller = new Unmarshaller(type);
        objMeta = unmarshaller.unmarshal(objFile);
        cdb = unmarshaller.getClassDatabase();

        //apply local updates
        for (String initialDelta : initialDeltas) {
            Delta delta = new Delta().deserialize(initialDelta);
            delta.getMessage().visit(this);
        }
        //TODO unmarshal the saved version
        //TODO apply the deltas to the model
        //TODO update the revision file
        return null;
    }

    @Override
    public void save() {
        reset(SimpleObjUpdater.toXmlObj(objMeta));
    }


    @Override
    public <T> T handleMessage(InMessage<ObjListener, T> inMessage) {
        //todo append to a file specific to this object
        return null;
    }

    private void closeDeltaWriter() {
        try {
            getDeltaWriter().flush();
            getDeltaWriter().close();
            deltaWriter = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputStreamWriter getDeltaWriter() {
        if(deltaWriter==null) {
            try {
                deltaWriter = new OutputStreamWriter(new FileOutputStream(deltaFile, true), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return deltaWriter;
    }

    public ObjectMeta getObjMeta() {
        return objMeta;
    }

    public ClassDatabase getCdb() {
        return cdb;
    }
}
