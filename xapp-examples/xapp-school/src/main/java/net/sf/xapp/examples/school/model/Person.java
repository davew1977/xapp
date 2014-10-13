package net.sf.xapp.examples.school.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
@ValidImplementations({Teacher.class, Pupil.class})
public interface Person extends FileSystem{
    @Key
    String getUsername();

    String getFirstName();

    String getSecondName();

    DirMeta getHomeDir();
}
