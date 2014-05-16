package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@ValidImplementations({Teacher.class, Pupil.class})
public interface Person {
    @Key
    String getUsername();

    String getFirstName();

    String getSecondName();

    DirMeta getHomeDir();
}
