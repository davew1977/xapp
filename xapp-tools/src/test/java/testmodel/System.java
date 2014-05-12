package testmodel;

import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.application.api.Launcher;
import net.sf.xapp.application.api.SimpleApplication;

import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class System {
    private Pupil pupilOfTheYear;
    private Teacher teacherOfTheYear;
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

    public static void main(String[] args) {
        Launcher.run(System.class, new SimpleApplication() {
            @Override
            public void handleUncaughtException(Throwable e) {
                e.printStackTrace();
            }
        }, "db-system.xml");
    }
}
