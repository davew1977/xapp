/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util;

import java.io.File;
import java.io.FilenameFilter;

public class RegexpFilenameFilter implements FilenameFilter
{
    private final String filePattern;

    public RegexpFilenameFilter(String filePattern)
    {
        this.filePattern = filePattern;
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return name.matches(filePattern);
    }
}
