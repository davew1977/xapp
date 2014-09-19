package net.sf.xapp.objserver;

import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 15/08/2014
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */
public class ObjInfo {
    private String className;
    private String key;
    private String fileName;
    private long revision;
    private ObjectMeta objectMeta;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void load(File dir) throws ClassNotFoundException {
        Class aClass = Class.forName(className);
        Unmarshaller unmarshaller = new Unmarshaller(aClass);
        unmarshaller.getClassDatabase().setMaster();
        objectMeta = unmarshaller.unmarshal(new File(dir, fileName()));
    }

    private String fileName() {
        return fileName != null ? fileName : key + ".xml";
    }

    @Transient
    public ObjectMeta getObjectMeta() {
        return objectMeta;
    }
}
