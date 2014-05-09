package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Namespace;
import net.sf.xapp.annotations.objectmodelling.Reference;

import java.util.HashMap;
import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@Namespace({Person.class, FileMeta.class})
public class School {
    private String name;
    private Teacher headTeacher;
    private Pupil starOfTheWeek;
    private Map<String, Person> people = new HashMap<String, Person>();
    private Map<String, ClassRoom> classRooms = new HashMap<String, ClassRoom>();

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Reference()
    public Teacher getHeadTeacher() {
        return headTeacher;
    }

    public void setHeadTeacher(Teacher headTeacher) {
        this.headTeacher = headTeacher;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, ClassRoom> getClassRooms() {
        return classRooms;
    }

    public void setClassRooms(Map<String, ClassRoom> classRooms) {
        this.classRooms = classRooms;
    }

    @Reference
    public Pupil getStarOfTheWeek() {
        return starOfTheWeek;
    }

    public void setStarOfTheWeek(Pupil starOfTheWeek) {
        this.starOfTheWeek = starOfTheWeek;
    }

    @Override
    public String toString() {
        return name;
    }
}
