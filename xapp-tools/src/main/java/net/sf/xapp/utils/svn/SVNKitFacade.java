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

import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Date: Nov 19, 2008
 * Time: 2:29:15 PM
 */
public class SVNKitFacade implements SVNFacade
{

	private String m_username;
    private String m_password;
    private SVNClientManager m_svnManager;

    /**
	 * Creates a facade and sets up authentication for subversion.
	 * Configured for HTTP.
	 *
	 * @param user svn username
	 * @param pwd svn password
	 */
	public SVNKitFacade(String user, String pwd) //SVNauth exception?
	{
		DAVRepositoryFactory.setup();
		m_username = user;
        m_password = pwd;

		DefaultSVNOptions options = new DefaultSVNOptions();
        options.setAuthStorageEnabled(false);
		m_svnManager = SVNClientManager.newInstance(options, user, pwd);
	}

	public SVNKitFacade() //SVNauth exception?
	{
		DAVRepositoryFactory.setup();
		DefaultSVNOptions options = new DefaultSVNOptions();
        options.setAuthStorageEnabled(false);
		m_svnManager = SVNClientManager.newInstance(options);
	}

    public SVNKitFacade(SvnConfig svnConfig)
    {
        this(svnConfig.getUsername(), svnConfig.getPassword());
    }

    /**
	 * Updates to head
	 *
	 * @param files the local filepath to update
	 */
	public UpdateResult update(File... files)
	{
		try
		{

            UpdateResult result = new UpdateResult();
            UpdateEventHandler eventHandler = new UpdateEventHandler(result);
            m_svnManager.setEventHandler(eventHandler);
            //update file, to revision, recursively, don't allow files not under version control to obstruct update, depth is not sticky
            long[] rev = m_svnManager.getUpdateClient().doUpdate(files, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
            result.setRev(rev[0]);
            m_svnManager.setEventHandler(null);
            return result;
		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
    }

	public Date getTimestamp(String filepath)
	{
		File[] file = new File[]{new File(filepath)};

		try
		{
			SVNInfo info = m_svnManager.getWCClient().doInfo(file[0], SVNRevision.HEAD);
			LogHandler handler = new LogHandler();
			String relativePath = info.getURL().toString().replace(info.getRepositoryRootURL().toString(), "");
			//take out a log for the repository, specified path, 1 peg revision, 1 start revision, head revision, don't stop on copy, don't discover changed paths, limit to 1 result only, and send to handler
			m_svnManager.getLogClient().doLog(info.getRepositoryRootURL(),new String[]{relativePath}, SVNRevision.create(1), SVNRevision.HEAD, SVNRevision.create(1), false, false, 1, handler);
			List<Date> timestamps = handler.getTimeStamps();
			if(timestamps.size()>1)
			{
				throw new RuntimeException("A log from HEAD to HEAD should only give one timestamp!");
			}
			else if(timestamps.size()<1)
			{
				throw new RuntimeException("There is no log at all for the supplied path! "+filepath);
			}
			return timestamps.get(0);
		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
	}

	/**
	 * Attempts to lock a given file, providing a lock message. Does not steal locks.
	 * @param filepath local filepath to the file to lock - note, this only takes specific files, not folders!
	 * @param lockmsg lock message for svn
	 * @return Whether the lock was taken or not (checked by calling {@link #hasOwnershipOfLock(String)} with param <code>filepath</code>})
	 */
	public boolean lock(String filepath, String lockmsg)
	{
		File file = new File(filepath);
		if(!file.isFile())
		{
			throw new RuntimeException(file.getAbsolutePath()+" is not a file!");
		}

		File[] path = new File[]{file};


		try
		{
			m_svnManager.getWCClient().doLock(path, false, lockmsg);

			return hasOwnershipOfLock(filepath);
		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
	}

	/**
	 * Releases the users own lock on the file without breaking any others'.
	 * @param filepath The filepath to unlock
	 */
	public void unlock(String filepath)
	{
		unlock(filepath, true);
	}


	/**
	 * Breaks the lock on a file, from another (or your own) user
	 * @param filepath The file to unlock
	 */
	public void breakLock(String filepath)
	{
		unlock(filepath, true);
	}

	private void unlock(String filepath, boolean force)
	{
		File[] path = new File[]{new File(filepath)};

		try
		{
			m_svnManager.getWCClient().doUnlock(path, force);
		}
		catch(Exception svne)
		{
			throw new RuntimeException(svne);
		}
	}

	public boolean lockExists(String filepath)
	{
		File file = new File(filepath);
		if(!file.isFile())
		{
			throw new RuntimeException(file.getAbsolutePath()+" is not a file!");
		}

		try
		{
						//check remote status for this file, same as "svn status -u"
			SVNStatus status = m_svnManager.getStatusClient().doStatus(file, true);

			return status.getRemoteLock() != null; // lock exists remotely
		}
		catch(Exception svne)
		{
			throw new RuntimeException(svne);
		}
	}

	/**
	 * Checks whether the registered user owns the lock on this file, matching both token and username.
	 *
	 * @param filepath The path to the local file
	 * @return Whether the user owns the lock on this file
	 */
	public boolean hasOwnershipOfLock(String filepath)
	{
		File file = new File(filepath);
		if(!file.isFile())
		{
			throw new RuntimeException(file.getAbsolutePath()+" is not a file!");
		}

		try
		{
			//check remote status for this file, same as "svn status -u"
			SVNStatus status = m_svnManager.getStatusClient().doStatus(file, true);

			return status.getLocalLock() != null // lock exists locally
				    && status.getRemoteLock() != null // lock exists remotely
					&& status.getRemoteLock().getOwner().equals(m_username) // our user owns the lock
					&& status.getRemoteLock().getID().equals(status.getLocalLock().getID()); //our token matches the lock

		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
	}

	public void revert(File... files)
	{

		try
		{
			//revert the path, recursively, without changelist
			m_svnManager.getWCClient().doRevert(files, SVNDepth.INFINITY, null);
		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
	}

    public void doImport(String svnpath, String localDirToAdd, boolean encodedURL, String commitMessage)
    {
        try
        {
            SVNURL url = (encodedURL ? SVNURL.parseURIEncoded(svnpath) : SVNURL.parseURIDecoded(svnpath));
            m_svnManager.getCommitClient().doImport(new File(localDirToAdd), url, commitMessage, null, false, false, SVNDepth.INFINITY);
        }
        catch (SVNException e)
        {
			throw new RuntimeException(e.getMessage(), e);
		}
    }

    public void export(String svnPath, String targetPath)
    {
        export(svnPath, targetPath, svnPath.startsWith("https"));
    }

    public long getRevision(String path)
    {
        try
        {
            return m_svnManager.getWCClient().doInfo(new File(path), SVNRevision.HEAD).getRevision().getNumber();

        }
        catch (SVNException e)
        {
			throw new RuntimeException(e);
		}
    }

    public String getUsername()
    {
        return m_username;
    }

    public boolean hasChangedSinceRevision(String workingFile, long r1)
    {
        return changedFiles(workingFile, r1, SVNRevision.HEAD).size() > 0;
    }

    public boolean hasChangedSinceRevision(String workingFile, long startRevision, long endRevision)
    {
        return changedFilesBetween(workingFile, startRevision, endRevision).size()>0;
    }

    public List<String> filesChangedSinceRevision(String workingFile, long r1)
    {
        return changedFiles(workingFile, r1, SVNRevision.HEAD);
    }

    public List<String> changedFilesBetween(String workingDirectory, long startRevision, long endRevision)
	{
		return changedFiles(workingDirectory, startRevision, SVNRevision.create(endRevision));
	}


	private List<String> changedFiles(String workingDirectory, long startRevision, SVNRevision endRevision)
	{
        //add one to start revision, or else the svn log will include it and will always indicate a difference
        startRevision+=1;
        List<String> changedPaths = new ArrayList<String>();
		try
		{
			LogHandler logHandler = new LogHandler();
			m_svnManager.getLogClient().doLog(new File[]{new File(workingDirectory)},
					SVNRevision.create(startRevision), endRevision,
					SVNRevision.UNDEFINED, //undefined peg revision
					false, //DO NOT stopOnCopy!
					true, true, 0, null, //discover changed paths, include merged revisions, no limit, no revision properties
					logHandler);

			List<String> files = logHandler.getChangedFiles();

			// tidy up among all the returned files that don't match our path of interest... The log command will return
			// all files changed for every revision it gets a hit on, even if only one file matches the path, that's why
			// we need to check for ourselves
			SVNInfo info = m_svnManager.getWCClient().doInfo(new File(workingDirectory), SVNRevision.HEAD);
			String pathOfInterest = info.getURL().getPath().replace(info.getRepositoryRootURL().getPath(), "");

			for(String s : files)
			{
				if(s.startsWith(pathOfInterest)) changedPaths.add(s);
			}
		}
	    catch(Exception e)
		{
			throw new RuntimeException(e);
		}

		return changedPaths;
	}


	/**
	 * Gets the revision at which the folder was created on svn (good for tracking branch creation!)
	 * @param workingPath The working path to check creation of
	 * @return The revision at which the folder was created, -1 if it couldn't be found
	 */
	public long getBranchCreationRevision(String workingPath)
	{
		try
		{
			LogHandler logHandler = new LogHandler();
			m_svnManager.getLogClient().doLog(new File[]{new File(workingPath)},
					SVNRevision.create(1), SVNRevision.HEAD,
					SVNRevision.UNDEFINED, //undefined peg revision
					true, //stopOnCopy
					true, false, 0, null, //discover changed paths, don't include merged revisions because it might freeze the call!, no limit, no revision properties
					logHandler);

			return logHandler.getOldestRevision();
		}
	    catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public boolean hasLocalChanges(File dir)
	{
		try
		{
			SimpleStatusHandler handler = new SimpleStatusHandler();
			m_svnManager.getStatusClient().doStatus(dir, null, SVNDepth.INFINITY, false, false, false, false, handler, null);
			return handler.isModified();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}


	public void export(String svnPath, String targetPath, boolean encodedURL)
    {
        try
        {
            SVNURL url = (encodedURL ? SVNURL.parseURIEncoded(svnPath) : SVNURL.parseURIDecoded(svnPath));
            m_svnManager.getUpdateClient().doExport(url, new File(targetPath), SVNRevision.HEAD, SVNRevision.HEAD, null, false, SVNDepth.INFINITY);
        }
        catch (SVNException e)
        {
			throw new RuntimeException(e);
		}

    }


    /**
	 * Commits to svn
     * @param message commit message to svn
     * @param files
     */
	public long commit(String message, File... files)
    {
        return commit(true, message, files);
    }

    public long commit(boolean keepLocks, String message, File... files)
	{
		try
		{
			//commit path, keep locks, assign message, don't force it, no revision properties, no changelist, don't keep changelist, don't force, commit recursively
            SVNCommitInfo commitInfo = m_svnManager.getCommitClient().doCommit(files, keepLocks, message, null, null, false, false, SVNDepth.INFINITY);
            return commitInfo.getNewRevision();
        }
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
    }



    /**
	 * Checks out head from svn
	 *
	 * @param svnpath where to check out from
	 * @param targetpath where to check out to
	 * @param encodedURL whether the svnpath is encoded
	 */
	public void checkout(String svnpath, String targetpath, boolean encodedURL)
	{
		checkout(svnpath, targetpath, encodedURL, false);
	}

    public void checkout(String svnpath, String targetpath)
    {
        checkout(svnpath, targetpath, svnpath.startsWith("https"));
    }

	/**
	 *
	 * @param svnpath Path to checkout
	 * @param targetpath Where to check it out to
	 * @param encodedURL Whether the svn path is encoded (https)
	 * @param empty Whether to checkout an empty directory (used for adding a new item to existing sources)
	 * 				Note that this might set a property so that you will not be able to update this folder
	 * 				later on - check SVNKit documentation.
	 */
	public void checkout(String svnpath, String targetpath, boolean encodedURL, boolean empty)
	{
		try
		{
			SVNURL url = (encodedURL ? SVNURL.parseURIEncoded(svnpath) : SVNURL.parseURIDecoded(svnpath));
			File target = new File(targetpath);
			SVNDepth depth = empty ? SVNDepth.EMPTY : SVNDepth.INFINITY;
			//checkout svn, to target, peg revision, wanted revision, recursively, don't allow unversioned obstructions
            m_svnManager.getUpdateClient().setEventHandler(new BasicEventHandler());
			m_svnManager.getUpdateClient().doCheckout(url, target, SVNRevision.HEAD, SVNRevision.HEAD, depth, false);
            m_svnManager.getUpdateClient().setEventHandler(null);
		}
		catch(SVNException svne)
		{
			throw new RuntimeException(svne);
		}
	}


	public void copy(String workingPath, String newPath)
	{
	    File workingCopy = new File(workingPath);
		SVNCopySource[] sourcePath = new SVNCopySource[]{new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, workingCopy)};
		File newCopy = new File(newPath);

		try
		{
			m_svnManager.getCopyClient().doCopy(sourcePath, newCopy, false, false, true);
		}
		catch (SVNException e)
		{
			throw new RuntimeException(e);
		}
	}

    public void move(String workingPath, String newPath) {
        File workingCopy = new File(workingPath);
        File targetFile = new File(newPath);
        try {
            m_svnManager.getMoveClient().setEventHandler(new BasicEventHandler());
            m_svnManager.getMoveClient().doMove(workingCopy, targetFile);
            m_svnManager.getMoveClient().setEventHandler(null);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

	public void delete(String workingPath)
	{
		File workingCopy = new File(workingPath);

		try
		{
			m_svnManager.getWCClient().doDelete(workingCopy, false, false);
        }
		catch (SVNException e)
		{
			throw new RuntimeException(e);
		}
	}

    public void deleteOnServer(String svnpath, boolean encodedURL)
    {
        try
        {
            SVNURL url = (encodedURL ? SVNURL.parseURIEncoded(svnpath) : SVNURL.parseURIDecoded(svnpath));
            m_svnManager.getCommitClient().doDelete(new SVNURL[]{url}, "deleted");
        }
        catch (SVNException e)
        {
            throw new RuntimeException(e);
        }

    }


    public void add(String path)
	{
		File location = new File(path);
		try
		{
			m_svnManager.getWCClient().doAdd(location, false, false, true, SVNDepth.INFINITY, false, false);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	
	public List<String> list(final String url)
	{
		try
		{
			SVNURL svnurl = url.contains("https") ? SVNURL.parseURIEncoded(url) : SVNURL.parseURIDecoded(url);
			ListHandler listHandler = new ListHandler(url);
			m_svnManager.getLogClient().doList(svnurl, SVNRevision.HEAD, SVNRevision.HEAD, false, false, listHandler);
			return listHandler.getList();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

    public String getBranchName(String workingcopy)
    {
        try
        {
            SVNInfo svnInfo = m_svnManager.getWCClient().doInfo(new File(workingcopy), SVNRevision.WORKING);
            String uri = svnInfo.getURL().toDecodedString();
            String[] chunks = uri.split("branches/");
            if(chunks.length==1)
            {
                return null;
            }
            else if(chunks.length>2)
            {
                throw new RuntimeException("\"branches/\" appears more than once in working copy uri: " + uri);
            }
            else
            {
                return chunks[1].split("/", 2)[0];
            }
        }
        catch (SVNException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class BasicEventHandler implements ISVNEventHandler {
        @Override
        public void handleEvent(SVNEvent event, double progress) throws SVNException {
            System.out.println(event);
        }

        @Override
        public void checkCancelled() throws SVNCancelException {

        }
    }

    class ListHandler implements ISVNDirEntryHandler
	{
		List<String> m_list;
		String m_url;

		public ListHandler(String url)
		{
			m_url = url;
			m_list = new ArrayList<String>();
		}

		public void handleDirEntry(SVNDirEntry svnDirEntry) throws SVNException
		{
			String location = m_url;
			String name = svnDirEntry.getName();
			if(m_url.charAt(m_url.length()-1) == '/')
			{
				location = m_url.substring(0,m_url.length()-1);     //remove trailing slash if any
			}
			if(!name.equals(location.substring(location.lastIndexOf("/")+1)))
			{
				m_list.add(name);
			}
		}

		public List<String> getList()
		{
			return m_list;
		}
	}

	class LogHandler implements ISVNLogEntryHandler
	{
		List<String> m_changedFiles;
		List<Long> m_revisions;
		List<Date> m_timeStamps;

		public LogHandler()
		{
			m_changedFiles = new ArrayList<String>();
			m_revisions = new ArrayList<Long>();
			m_timeStamps = new ArrayList<Date>();
		}

		public void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException
		{
			m_revisions.add(svnLogEntry.getRevision());
			for(Object objPath : svnLogEntry.getChangedPaths().keySet())
			{
				String path = (String) objPath;
				if(!m_changedFiles.contains(path))
				{
					m_changedFiles.add(path);
				}
			}
			m_timeStamps.add(svnLogEntry.getDate());
		}

		public List<String> getChangedFiles()
		{
			return m_changedFiles;
		}

		public long getNewestRevision()
		{
			long rev = -1;
			for(Long r : m_revisions)
			{
				if(r > rev) rev = r;
			}
			return rev;
		}

		public long getOldestRevision()
		{
			long rev = -1;
			for(Long r : m_revisions)
			{
				if(r < rev || rev == -1) rev = r;
			}
			return rev;
		}

		public List<Long> getRevisions()
		{
			return m_revisions;
		}

		public List<Date> getTimeStamps()
		{
			return m_timeStamps;
		}
	}

    public String getPassword()
    {
        return m_password;
    }

    private class SimpleStatusHandler implements ISVNStatusHandler
	{
		boolean m_modified;

		public void handleStatus(SVNStatus status) throws SVNException
		{
			boolean statusModified = (status.getContentsStatus() != SVNStatusType.STATUS_NORMAL
							&& status.getContentsStatus() != SVNStatusType.STATUS_UNVERSIONED);
			m_modified = m_modified || statusModified;

			if (statusModified)
			{
				System.out.println("Modified: "+status.getFile().getAbsolutePath());
			}
		}

		public boolean isModified()
		{
			return m_modified;
		}
	}
}
