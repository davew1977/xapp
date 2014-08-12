package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@NamespaceFor(FileMeta.class)
public abstract class AbstractPerson implements Person {
    private String username;
    private String firstName;
    private String secondName;
    private TextFile aboutMe;
    private DirMeta homeDir = new DirMeta("docs");
    private PersonSettings personSettings;

    @Override
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

    @Override
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    @Override
    public DirMeta getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(DirMeta homeDir) {
        this.homeDir = homeDir;
    }

    public PersonSettings getPersonSettings() {
        return personSettings;
    }

    public void setPersonSettings(PersonSettings personSettings) {
        this.personSettings = personSettings;
    }

    @Override
    public String toString() {
        return username;
    }
}
