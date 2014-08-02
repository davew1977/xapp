package net.sf.xapp.utils.svn;

import org.tmatesoft.svn.core.wc.SVNEvent;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 30/07/14
 * Time: 07:14
 * To change this template use File | Settings | File Templates.
 */
public class DoNothingConflictHandler implements ConflictHandler {
    @Override
    public boolean handle(SVNEvent event) {
        return false;
    }
}
