/*
 *
 * Date: 2009-dec-14
 * Author: davidw
 *
 */
package net.sf.xapp.utils.svn;

import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.SVNCancelException;

import static org.tmatesoft.svn.core.wc.SVNStatusType.*;

public class UpdateEventHandler implements ISVNEventHandler
{
    private UpdateResult m_updateResult;
    private ConflictHandler conflictHandler;

    public UpdateEventHandler(UpdateResult updateResult, ConflictHandler conflictHandler)
    {
        m_updateResult = updateResult;
        this.conflictHandler = conflictHandler;
    }

    /*
    * progress  is  currently  reserved  for future purposes and now is always
    * ISVNEventHandler.UNKNOWN
    */
    public void handleEvent(SVNEvent event, double progress)
    {
        /*
         * Gets the current action. An action is represented by SVNEventAction.
         * In case of an update an  action  can  be  determined  via  comparing
         * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants.
         */
        SVNEventAction action = event.getAction();
        String pathChangeType = " ";
        if (action == SVNEventAction.UPDATE_ADD)
        {
            /*
             * the item was added
             */
            pathChangeType = "A";
        }
        else if (action == SVNEventAction.UPDATE_DELETE)
        {
            /*
             * the item was deleted
             */
            pathChangeType = "D";
        }
        else if (action == SVNEventAction.UPDATE_UPDATE)
        {
            /*
             * Find out in details what  state the item is (after  having  been
             * updated).
             *
             * Gets  the  status  of  file/directory  item   contents.  It   is
             * SVNStatusType  who contains information on the state of an item.
             */
            SVNStatusType contentsStatus = event.getContentsStatus();
            if (contentsStatus == CHANGED)
            {
                /*
                 * the  item  was  modified in the repository (got  the changes
                 * from the repository
                 */
                pathChangeType = "U";
            }
            else if (contentsStatus == CONFLICTED)
            {
                /*
                 * The file item is in  a  state  of Conflict. That is, changes
                 * received from the repository during an update, overlap  with
                 * local changes the user has in his working copy.
                 */
                pathChangeType = "C";
                Conflict e = new Conflict(event.getFile(), event.getPreviousRevision(), event.getRevision());
                m_updateResult.getConflicts().add(e);
                boolean handled = conflictHandler.handle(event);
                if(handled) {
                    m_updateResult.setConflictsHandled();
                }
            }
            else if (contentsStatus == MERGED)
            {
                /*
                 * The file item was merGed (those  changes that came from  the
                 * repository  did  not  overlap local changes and were  merged
                 * into the file).
                 */
                pathChangeType = "G";
            }
        }
        else if (action == SVNEventAction.UPDATE_EXTERNAL)
        {
            /*for externals definitions*/
            System.out.println("Fetching external item into '"
                    + event.getFile().getAbsolutePath() + "'");
            System.out.println("External at revision " + event.getRevision());
            return;
        }
        else if (action == SVNEventAction.UPDATE_COMPLETED)
        {
            /*
             * Updating the working copy is completed. Prints out the revision.
             */
            System.out.println("At revision " + event.getRevision());
            return;
        }
        else if (action == SVNEventAction.ADD)
        {
            System.out.println("A     " + event.getFile());
            return;
        }
        else if (action == SVNEventAction.DELETE)
        {
            System.out.println("D     " + event.getFile());
            return;
        }
        else if (action == SVNEventAction.LOCKED)
        {
            System.out.println("L     " + event.getFile());
            return;
        }
        else if (action == SVNEventAction.LOCK_FAILED)
        {
            System.out.println("failed to lock    " + event.getFile());
            return;
        }

        /*
        * Now getting the status of properties of an item. SVNStatusType  also
        * contains information on the properties state.
        */
        SVNStatusType propertiesStatus = event.getPropertiesStatus();
        /*
         * At first consider properties are normal (unchanged).
         */
        String propertiesChangeType = " ";
        if (propertiesStatus == CHANGED)
        {
            /*
             * Properties were updated.
             */
            propertiesChangeType = "U";
        }
        else if (propertiesStatus == CONFLICTED)
        {
            /*
             * Properties are in conflict with the repository.
             */
            propertiesChangeType = "C";
        }
        else if (propertiesStatus == MERGED)
        {
            /*
             * Properties that came from the repository were  merged  with  the
             * local ones.
             */
            propertiesChangeType = "G";
        }

        /*
        * Gets the status of the lock.
        */
        String lockLabel = " ";
        SVNStatusType lockType = event.getLockStatus();

        if (lockType == LOCK_UNLOCKED)
        {
            /*
             * The lock is broken by someone.
             */
            lockLabel = "B";
        }

        System.out.println(pathChangeType
                + propertiesChangeType
                + lockLabel
                + "       "
                + event.getFile());
    }

    /*
    * Should be implemented to check if the current operation is cancelled. If
    * it is, this method should throw an SVNCancelException.
    */
    public void checkCancelled() throws SVNCancelException
    {
    }

}
