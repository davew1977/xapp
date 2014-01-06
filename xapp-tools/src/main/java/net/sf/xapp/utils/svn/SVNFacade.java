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
package net.sf.xapp.utils.svn;

import java.io.CharArrayWriter;
import java.util.Date;
import java.util.List;

/**
 */
public interface SVNFacade
{
	/**
	 * Updates to head
	 *
	 * @param filepath the local filepath to update
	 */
	UpdateResult update(String filepath);

	/**
	 * Gets the latest time of change for the given file/directory. Note that this is dependent on server time.
	 * @param filepath The path to check time for
	 * @return A Date representing the time of change
	 */
	public Date getTimestamp(String filepath);

	/**
	 * Attempts to lock a given file, providing a lock message. Does not steal locks. Locking something that the user
	 * already owns will result in it behaving like a successful lock.
	 * @param filepath local filepath to the file to lock - note, this only takes specific files, not folders!
	 * @param lockmsg lock message for svn
	 * @return Whether the lock was taken or not
	 */
	boolean lock(String filepath, String lockmsg);

	/**
	 * Releases the users own lock on the file without breaking any others'.
	 * @param filepath The filepath to unlock
	 */
	void unlock(String filepath);

	/**
	 * Breaks the lock on a file, from another (or your own) user
	 * @param filepath The file to unlock
	 */
	public void breakLock(String filepath);

	/**
	 * Checks for remote lock on the given file
	 * @param filepath file to check lock on
	 * @return whether lock exists. May return an incorrect "true" if operation throws exception.
	 */
	public boolean lockExists(String filepath);

	/**
	 * Checks whether the registered user owns the lock on this file, matching both token and username.
	 *
	 * @param filepath The path to the local file
	 * @return Whether the user owns the lock on this file
	 */
	boolean hasOwnershipOfLock(String filepath);

	/**
	 * Commits to svn
	 * @param filepath local filepath
     * @param message commit message to svn
     */
	long commit(String filepath, String message, boolean keepLocks);
	long commit(String filepath, String message);


	/**
	 *
	 * @param svnpath Path to checkout
	 * @param targetpath Where to check it out to
	 * @param encodedURL Whether the svn path is encoded (https)
	 * @param empty Whether to checkout an empty directory (used for adding a new item to existing sources)
	 * 				Note that this might set a property so that you will not be able to update this folder
	 * 				later on - check SVNKit documentation.
	 */
	void checkout(String svnpath, String targetpath, boolean encodedURL, boolean empty);
	void checkout(String svnpath, String targetpath, boolean encodedURL);
	void checkout(String svnpath, String targetpath);

	/**
	 * Reverts the given target recursively
	 * @param path The path to revert
	 */
	void revert(String path);

    /**
     * adds some files directly to svn. No working copies
     * @param svnpath svn url
     * @param localDirToAdd to add
     * @param encodedURL http or https
     * @param commitMessage dwisott
     */
    void doImport(String svnpath, String localDirToAdd, boolean encodedURL, String commitMessage);

    /**
     * gets from svn without creating working copies
     * @param svnPath svn url
     * @param targetPath local dir to place files
     * @param encodedURL http or https
     */
    void export(String svnPath, String targetPath, boolean encodedURL);

    /**
     * simple version of method where encodeUrl=svnPath.startsWith("https")
     * @param svnPath svn url
     * @param targetPath local dir to place files
     */
    void export(String svnPath, String targetPath);

    /**
     * retrieves info from working copy
     * TODO return own class to hide SVNKit dependency from Facade
     * @param workingCopy
     * @return info on working copy
     */
    long getRevision(String workingCopy);

    String getUsername();

    /**
     * detect if a file or dir has changes since the specified revision (exclusive)
     * @param workingFile the file or directory
     * @param r1 the revision to compare with
     * @return
     */
    boolean hasChangedSinceRevision(String workingFile, long r1);

    /**
     * detect if a file or dir has changes since the specified revision (exclusive)
     * @param workingFile the file or directory
     * @param r1 the revision to compare with
     * @return
     */
    boolean hasChangedSinceRevision(String workingFile, long startRevision, long endRevision);


    /**
     * find the files changed in a directory since the specified revision (exclusive)
     *
     * The method will work if supplied with a non-directory file, but semantically one would
     * expect to pass in a directory
     *
     * @param workingFile the directory
     * @param r1 the revision to compare with
     * @return
     */
    List<String> filesChangedSinceRevision(String workingFile, long r1);


	List<String> changedFilesBetween(String workingDirectory, long startRevision, long endRevision);


	/**
	 * Makes a local copy of the working path, which is added to subversion. This does not
	 * have remote effect until a commit is done.
	 * Note that the parental folders have to exist for the newPath, but the leaf in the path must NOT exist,
	 * as this is the name which the copy will be given.
	 *
	 * @param workingPath The path to copy
	 * @param newPath Where to copy to
	 */
	void copy(String workingPath, String newPath);

	/**
	 * Deletes a working copy - does not force
	 *
	 * @param workingPath The path to be removed
	 */
	void delete(String workingPath);

    void deleteOnServer(String svnPath, boolean encodedURL);


    /**
	 * Adds something to the repository. Climbs unversioned parents, adding them as well. Works recursively on directories.
	 * Does not force addition of already versioned files - this will throw an error instead.
	 *
	 * @param path The path to add to the repository
	 */
	void add(String path);


	/**
	 * Fetches a simple list of entries in the specified directory (url)
	 * @param url Which directory to list the contents of
	 * @return List of files and folders
	 */
	public List<String> list(final String url);


    /**
     * Gets the branch name of the svn item. Relies on the svn branch naming convention is followed, i.e. that
     * modules have branches,tags and trunk folders
     * @param workingcopy
     * @return null if trunk
     */
    String getBranchName(String workingcopy);
    long getBranchCreationRevision(String workingPath);

	boolean hasLocalChanges(String dir);

    String getPassword();
}
