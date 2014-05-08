package testmodel;

import net.sf.xapp.annotations.objectmodelling.Namespace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:25 AM
 * To change this template use File | Settings | File Templates.
 */
@Namespace(FileMeta.class)
public class DirMeta extends FileMeta {
    private List<FileMeta> files;

    public List<FileMeta> getFiles() {
        return files;
    }

    public void setFiles(List<FileMeta> files) {
        this.files = files;
    }
}
