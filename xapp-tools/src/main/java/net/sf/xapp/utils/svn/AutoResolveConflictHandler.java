package net.sf.xapp.utils.svn;

import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNEvent;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 30/07/14
 * Time: 07:14
 * To change this template use File | Settings | File Templates.
 */
public class AutoResolveConflictHandler implements ConflictHandler {
    private SVNKitFacade svnFacade;
    private SVNConflictChoice conflictChoice;

    public AutoResolveConflictHandler(SVNKitFacade svnFacade, SVNConflictChoice conflictChoice) {
        this.svnFacade = svnFacade;
        this.conflictChoice = conflictChoice;
    }

    @Override
    public boolean handle(SVNEvent event) {
        svnFacade.resolve(event.getFile(), conflictChoice);
        return true;
    }
}
