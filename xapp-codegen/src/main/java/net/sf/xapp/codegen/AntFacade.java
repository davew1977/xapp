/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;

public class AntFacade
{
    private Project project;

    public AntFacade()
    {
        project = new Project();
    }

    public void deleteFile(File file)
    {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setFile(file);
        delete.execute();
    }

    public void deleteDir(File file)
    {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(file);
        delete.execute();
    }
}
