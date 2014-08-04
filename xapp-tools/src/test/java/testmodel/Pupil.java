package testmodel;

import net.sf.xapp.annotations.objectmodelling.Reference;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
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