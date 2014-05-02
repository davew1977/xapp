package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@ValidImplementations({Teacher.class, Pupil.class})
public class Person {
   private String username;
   private String firstName;
   private String secondName;

    @Key
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return username;
    }
}
