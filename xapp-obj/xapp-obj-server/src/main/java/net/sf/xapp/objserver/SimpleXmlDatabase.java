package net.sf.xapp.objserver;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 15/08/2014
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class SimpleXmlDatabase {
    private File dir;

    public SimpleXmlDatabase(File dir) {
        this.dir = dir;
    }

    public List<ObjInfo> findAll() throws ClassNotFoundException {
        File[] objInfoFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".meta");
            }
        });
        List<ObjInfo> result = new ArrayList<ObjInfo>();
        Unmarshaller<ObjInfo> unmarshaller = new Unmarshaller<ObjInfo>(ObjInfo.class);
        for (File objInfoFile : objInfoFiles) {
            ObjInfo objInfo = unmarshaller.unmarshal(objInfoFile).getInstance();
            objInfo.load(dir);
            result.add(objInfo);
        }
        return result;
    }
}
