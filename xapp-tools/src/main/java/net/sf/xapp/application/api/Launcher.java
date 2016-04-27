/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.application.api;

import net.sf.xapp.application.core.ApplicationContainerImpl;
import net.sf.xapp.application.core.DefaultGUIContext;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.utils.Credentials;
import net.sf.xapp.utils.svn.SVNFacade;
import net.sf.xapp.utils.svn.SVNKitFacade;
import net.sf.xapp.utils.svn.SvnConfig;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class Launcher {

    public static ApplicationContainer run(Class rootClass) throws FileNotFoundException {
        return run(rootClass, new SimpleApplication(), null);
    }

    public static ApplicationContainer run(Class rootClass, String fileName) throws FileNotFoundException {
        return run(rootClass, new SimpleApplication(), fileName);
    }

    public static ApplicationContainer run(Class rootClass, final Application application) throws FileNotFoundException {
        return run(rootClass, application, null);
    }

    public static ApplicationContainer run(Class rootClass, final Application application, String fileNameOrURL) throws FileNotFoundException {
        return run(rootClass, application, fileNameOrURL, InspectionType.METHOD);
    }

    public static ApplicationContainer run(Class rootClass, final Application application, String fileNameOrURL, InspectionType inspectionType) throws FileNotFoundException {
        final ClassDatabase classDatabase = new ClassModelManager(rootClass, inspectionType);
        ClassModel classModel = classDatabase.getRootClassModel();
        final ObjectMeta rootObj;
        File file = null;
        if (fileNameOrURL == null) {
            rootObj = classModel.newInstance(null, true, false);
        }
        else {
            Unmarshaller unmarshaller = new Unmarshaller(classModel);
            if (fileNameOrURL.startsWith("classpath://")) {
                rootObj = unmarshaller.unmarshal(Launcher.class.getResourceAsStream(fileNameOrURL.substring("classpath://".length())));
                //no writable file in this case
            }
            else {
                file = new File(fileNameOrURL);
                rootObj = unmarshaller.unmarshal(file);
            }
        }
        return run(application, classDatabase, rootObj, file);
    }

    public static ApplicationContainer edit(Object obj, File file, Application application) {
        throw new UnsupportedOperationException(); //todo fix
        //return run(application, new ClassModelManager(obj.getClass(), InspectionType.FIELD), obj, file);
    }

    public static ApplicationContainer edit(Object obj) {
        return edit(obj, null, new SimpleApplication());
    }

    public static ApplicationContainer edit(Object obj, Application application) {
        return edit(obj, null, application);
    }

    public static ApplicationContainer run(final Application application, final ClassDatabase classDatabase, final ObjectMeta rootObj, File file) {
        try {
            final ApplicationContainerImpl[] applicationContainer = new ApplicationContainerImpl[1];
            final File file1 = file;
            Runnable launch = new Runnable() {
                public void run() {
                    applicationContainer[0] = new ApplicationContainerImpl(new DefaultGUIContext(file1, classDatabase, rootObj));
                    applicationContainer[0].setUserGUI(application);
                    applicationContainer[0].getMainFrame().setVisible(true);
                    applicationContainer[0].getMainFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                launch.run();
            }
            else {
                SwingUtilities.invokeAndWait(launch);
            }
            return applicationContainer[0];
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static SVNFacade createSVNFacade() {
        return createSVNFacade(new SvnConfig(), false);
    }

    public static SVNFacade createSVNFacade(SvnConfig svnConfig) {
        return createSVNFacade(svnConfig, false);
    }


    public static SVNFacade createSVNFacade(boolean forceCredentials) {
        return createSVNFacade(new SvnConfig(), forceCredentials);
    }

    public static SVNFacade createSVNFacade(SvnConfig svnConfig, boolean forceCredentials) {
        String username = svnConfig.getUsername();
        String password = svnConfig.getPassword();
        String[] creds = Boolean.getBoolean("skip.dialog") ?
                new String[]{username, password} :
                Credentials.obtainCredentials(null, username, password, forceCredentials);

        username = creds[0];
        password = creds[1];

        return new SVNKitFacade(username, password);
    }
}
