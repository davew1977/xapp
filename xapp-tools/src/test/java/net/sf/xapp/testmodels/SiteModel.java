package net.sf.xapp.testmodels;

import net.sf.xapp.application.api.Launcher;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.objectmodelling.api.InspectionType;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by oldDave on 07/07/2015.
 */
public class SiteModel {
    private List<Site> sites;

    public static void main(String[] args) throws FileNotFoundException {
        String filename = "/Users/oldDave/dev/xapp/xapp-tools/src/test/resources/net/sf/xapp/testmodels/sitemodel.xml";
        Launcher.run(SiteModel.class, new SimpleApplication(), filename, InspectionType.FIELD);
    }
}
