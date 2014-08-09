package testmodel;

import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@NamespaceFor(FileMeta.class)
public class Pupil extends AbstractPerson {
    private FileSystem anotherFileSystem;

    @Reference
    public FileSystem getAnotherFileSystem() {
        return anotherFileSystem;
    }

    public void setAnotherFileSystem(FileSystem anotherFileSystem) {
        this.anotherFileSystem = anotherFileSystem;
    }
}
