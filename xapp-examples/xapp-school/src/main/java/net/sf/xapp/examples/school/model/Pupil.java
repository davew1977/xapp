package net.sf.xapp.examples.school.model;

import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;

/**
 * © Webatron Ltd
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
