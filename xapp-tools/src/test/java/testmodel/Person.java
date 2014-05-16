package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@ValidImplementations({Teacher.class, Pupil.class})
@NamespaceFor(FileMeta.class)
public class Person {
    private String username;
    private String firstName;
    private String secondName;
    private TextFile aboutMe;
    private DirMeta homeDir = new DirMeta("docs");

    @Key
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Reference
    public TextFile getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(TextFile aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public DirMeta getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(DirMeta homeDir) {
        this.homeDir = homeDir;
    }

    @Override
    public String toString() {
        return username;
    }
}
