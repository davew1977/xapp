package net.sf.xapp.examples.school;

import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Launcher;

import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class SchoolSystem {
    private Pupil pupilOfTheYear;
    private Teacher teacherOfTheYear;
    private Person personOfTheYear;
    private Map<String, School> schools;

    public Map<String, School> getSchools() {
        return schools;
    }

    public void setSchools(Map<String, School> schools) {
        this.schools = schools;
    }

    @Reference
    public Pupil getPupilOfTheYear() {
        return pupilOfTheYear;
    }

    public void setPupilOfTheYear(Pupil pupilOfTheYear) {
        this.pupilOfTheYear = pupilOfTheYear;
    }

    @Reference
    public Teacher getTeacherOfTheYear() {
        return teacherOfTheYear;
    }

    public void setTeacherOfTheYear(Teacher teacherOfTheYear) {
        this.teacherOfTheYear = teacherOfTheYear;
    }

    @Reference
    public Person getPersonOfTheYear() {
        return personOfTheYear;
    }

    public void setPersonOfTheYear(Person personOfTheYear) {
        this.personOfTheYear = personOfTheYear;
    }

    public static void main(String[] args) {
        ApplicationContainer appContainer = Launcher.run(SchoolSystem.class, new SchoolApp() {
            @Override
            public void handleUncaughtException(Throwable e) {
                e.printStackTrace();
            }
        }, "db-system.xml");

        SchoolSystem instance = (SchoolSystem) appContainer.getGuiContext().getInstance();
        java.lang.System.out.println(instance.getSchools().get("Alfriston School").getClassRooms().get("Ruby").getHomeDir().getFiles());
    }
}
