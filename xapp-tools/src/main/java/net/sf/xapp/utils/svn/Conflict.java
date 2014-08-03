/*
 *
 * Date: 2009-dec-14
 * Author: davidw
 *
 */
package net.sf.xapp.utils.svn;

import org.tmatesoft.svn.core.wc.SVNEvent;

import java.io.File;

public class Conflict
{
    private SVNEvent svnEvent;
    private boolean handled;

    public Conflict(SVNEvent event)
    {
        this.svnEvent = event;
    }

    public SVNEvent getSvnEvent() {
        return svnEvent;
    }

    public void handle(ConflictHandler conflictHandler) {
        handled = conflictHandler.handle(svnEvent);
    }

    public boolean isHandled() {
        return handled;
    }
}
