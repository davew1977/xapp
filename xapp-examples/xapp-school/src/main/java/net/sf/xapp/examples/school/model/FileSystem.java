package net.sf.xapp.examples.school.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
@ValidImplementations({ClassRoom.class, Person.class})
public interface FileSystem {
    DirMeta getHomeDir();
}
