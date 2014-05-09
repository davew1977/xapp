package testmodel;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.ContainsReferences;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;

import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@Container(listProperty = "Pupils")
public class ClassRoom {
    private String name;
    private Teacher teacher;
    private List<Pupil> pupils;

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Reference
    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    @ContainsReferences
    public List<Pupil> getPupils() {
        return pupils;
    }

    public void setPupils(List<Pupil> pupils) {
        this.pupils = pupils;
    }

    @Override
    public String toString() {
        return name;
    }
}
