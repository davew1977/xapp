/*
 *
 * Date: 2009-dec-14
 * Author: davidw
 *
 */
package net.sf.xapp.utils.svn;

import java.io.File;

public class Conflict
{
    private File m_conflictedFile;
    private File m_mine;
    private File m_base;
    private File m_theirs;

    public Conflict(File conflictedFile, long previousRev, long rev)
    {
        m_conflictedFile = conflictedFile;
        m_mine = new File(conflictedFile.getParentFile(), conflictedFile.getName() + ".mine");
        m_base = new File(conflictedFile.getParentFile(), conflictedFile.getName() + "." + previousRev);
        m_theirs = new File(conflictedFile.getParentFile(), conflictedFile.getName() + "." + rev);
    }

    public File getConflictedFile()
    {
        return m_conflictedFile;
    }

    public File getMine()
    {
        return m_mine;
    }

    public File getBase()
    {
        return m_base;
    }

    public File getTheirs()
    {
        return m_theirs;
    }

    @Override
    public String toString() {
        return "Conflict{" +
                "m_conflictedFile=" + m_conflictedFile +
                ", m_mine=" + m_mine +
                ", m_base=" + m_base +
                ", m_theirs=" + m_theirs +
                '}';
    }
}
