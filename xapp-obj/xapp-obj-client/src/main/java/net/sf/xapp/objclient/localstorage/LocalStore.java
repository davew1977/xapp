package net.sf.xapp.objclient.localstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import net.sf.xapp.application.strategies.SaveStrategy;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objcommon.SimpleObjUpdater;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.types.Delta;
import net.sf.xapp.objserver.types.XmlObj;
import net.sf.xapp.utils.FileUtils;
import net.sf.xapp.utils.ant.AntFacade;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class LocalStore implements SaveStrategy{
    public static final String LOCAL_DIR = System.getProperty("user.home", ".") + "/xapp-cache";
    private final File dir;
    private final File revFile;
    private final File objFile;
    private final File deltaFile;
    private final String[] initialDeltas;
    private OutputStreamWriter deltaWriter;

    private ObjectMeta objMeta;
    private ClassDatabase cdb;

    public LocalStore(String user, String appId, String objId) {
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

    public void reconstruct(Class type, List<Delta> deltas, Long revTo) {
        Unmarshaller unmarshaller = new Unmarshaller(type);
        objMeta = unmarshaller.unmarshal(objFile);
        cdb = unmarshaller.getClassDatabase();

        SimpleObjUpdater objUpdater = new SimpleObjUpdater(objMeta);

        //apply local updates
        for (String initialDelta : initialDeltas) {
            Delta delta = new Delta().deserialize(initialDelta);
            delta.getMessage().visit(objUpdater);
        }
        //apply server updates (since our previous session ended)
        for (Delta delta : deltas) {
            delta.getMessage().visit(objUpdater);
        }
        FileUtils.writeFile(revTo, objFile);
    }

    @Override
    public void save() {
        reset(SimpleObjUpdater.toXmlObj(objMeta));
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
