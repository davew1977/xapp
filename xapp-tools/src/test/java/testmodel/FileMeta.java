package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:24 AM
 * To change this template use File | Settings | File Templates.
 */
@ValidImplementations({DirMeta.class, TextFile.class})
public class FileMeta {
    private String name;

    public FileMeta(String name) {
        this.name = name;
    }

    public FileMeta() {
    }

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
