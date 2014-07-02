package testmodel;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:25 AM
 * To change this template use File | Settings | File Templates.
 */
@NamespaceFor(FileMeta.class)
@Container(listProperty = "Files")
public class DirMeta extends FileMeta {
    private List<FileMeta> files;

    public DirMeta(String name) {
        super(name);
    }

    public DirMeta() {
    }

    public List<FileMeta> getFiles() {
        return files;
    }

    public void setFiles(List<FileMeta> files) {
        this.files = files;
    }

    @Override
    public DirMeta clone() throws CloneNotSupportedException {
        DirMeta dirMeta = (DirMeta) super.clone();
        if(files != null) {
            dirMeta.files  = new ArrayList<FileMeta>();
            for (FileMeta file : files) {
                dirMeta.files.add(file.clone());
            }
        }
        return dirMeta;
    }
}
