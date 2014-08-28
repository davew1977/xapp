package net.sf.xapp.examples.school.model;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@ValidImplementations({ClassRoom.class, Person.class})
public interface FileSystem {
    DirMeta getHomeDir();
}
