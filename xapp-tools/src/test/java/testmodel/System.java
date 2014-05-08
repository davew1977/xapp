package testmodel;

import net.sf.xapp.application.api.Launcher;
import net.sf.xapp.application.api.SimpleApplication;

import java.util.Map;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class System {
    private Map<String, School> schools;

    public Map<String, School> getSchools() {
        return schools;
    }

    public void setSchools(Map<String, School> schools) {
        this.schools = schools;
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
