package testmodel;

import net.sf.xapp.annotations.objectmodelling.Namespace;
import net.sf.xapp.annotations.objectmodelling.Reference;

import java.util.HashMap;
import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
@Namespace(Person.class)
public class School {
    private String name;
    private Teacher headTeacher;
    private Map<String, Person> people = new HashMap<String, Person>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Reference(local = true)
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

    @Override
    public String toString() {
        return name;
    }
}
