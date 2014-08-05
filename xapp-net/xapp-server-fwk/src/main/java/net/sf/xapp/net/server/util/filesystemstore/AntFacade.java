package net.sf.xapp.net.server.util.filesystemstore;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;

/**
 * Facade for Ant.
 * Using Ant's deletion algorithm here because File.delete() does not work very reliably, especially on Windows.
 * If the ant implementation does not throw an Exception, we can be sure it deleted the file.
 */
public class AntFacade
{
    private Project project;

    public AntFacade()
    {
        project = new Project();
    }

    public void deleteDir(File file)
    {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(file);
        delete.execute();
    }

    public void deleteFile(File file)
    {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setFile(file);
        delete.execute();
    }
}
