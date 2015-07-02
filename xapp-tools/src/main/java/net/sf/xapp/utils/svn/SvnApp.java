/*
 * Copyright 2007 bwin games AB
 *
 * Date: 2008-maj-09
 * Author: davidw
 *
 */
package net.sf.xapp.utils.svn;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.application.utils.SwingUtils;
import org.tmatesoft.svn.core.SVNException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;

import static net.sf.xapp.application.api.SimpleTreeGraphics.loadImage;

public abstract class SvnApp<T> extends SimpleApplication<T> {

    public static final ImageIcon UPDATE_ICON = loadImage("/update.png");
    public static final ImageIcon COMMIT_ICON = loadImage("/commit.png");
    public static final ImageIcon REVERT_ICON = loadImage("/revert.png");
    private UpdateAction m_updateAction = new UpdateAction();
    private CommitAction m_commitAction = new CommitAction();
    private RevertAction m_revertAction = new RevertAction();
    private SVNFacade svnFacade;

    public SvnApp(SVNFacade svnFacade)
    {
        this.svnFacade = svnFacade;
    }

    @Override
    public void init(ApplicationContainer<T> applicationContainer)
    {
        super.init(applicationContainer);

        setupToolbar();
    }

    protected void setupToolbar()
    {

        if (isSVNMode())
        {
            appContainer.getToolBar().add(m_updateAction).setToolTipText("Fetch changes from the server");
            appContainer.getToolBar().add(m_commitAction).setToolTipText("Saves and sends your changes to the server");
            appContainer.getToolBar().add(m_revertAction).setToolTipText("Removes all your changes since your last commit");
            updateViewState();
            appContainer.addBeforeHook(DefaultAction.QUIT, new ExitCommitHook());
            Box b = Box.createHorizontalBox();
            b.add(Box.createHorizontalStrut(10));
            b.add(new JLabel("user: " + svnFacade.getUsername()));
            SwingUtils.setFont(b);
            appContainer.getToolBar().add(b);
        }
    }

    private void updateViewState()
    {
        m_commitAction.setEnabled(isSVNMode());
        m_revertAction.setEnabled(isSVNMode());
        m_updateAction.setEnabled(isSVNMode());
    }

    private boolean isSVNMode()
    {
        return svnFacade != null;
    }

    private class CommitAction extends AbstractAction
    {
        private CommitAction()
        {
            super("Commit", COMMIT_ICON);
        }

        public void actionPerformed(ActionEvent e)
        {
            trySave();
            if(svnFacade.hasLocalChanges(currentFile())) {
                String commitMessage = getCommitMessage();
                if (commitMessage!=null)
                {
                    commit(commitMessage);
                    SwingUtils.warnUser(appContainer.getMainFrame(), "Commit successful");
                }
            } else {
                SwingUtils.warnUser(appContainer.getMainFrame(), "No Changes to Commit");
            }
        }
    }

    private String getCommitMessage() {
        JTextArea textarea = new JTextArea();
        textarea.setWrapStyleWord(true);
        textarea.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(textarea);
        jsp.setPreferredSize(new Dimension(300,200));
        int i = JOptionPane.showConfirmDialog(appContainer.getMainFrame(),
                jsp, "Enter a message to commit your changes:", JOptionPane.OK_CANCEL_OPTION);
        String message = textarea.getText();
        message = message==null ? "changes" : message;
        return i == JOptionPane.OK_OPTION ? message : null;
    }

    private class RevertAction extends AbstractAction
    {
        private RevertAction()
        {
            super("Revert", REVERT_ICON);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (SwingUtils.askUser(appContainer.getMainFrame(), "Are you sure you want to undo all changes\nsince your last commit?"))
            {
                svnFacade.revert(svnFiles());

                reloadFile();
            }
        }

    }


    private class UpdateAction extends AbstractAction
    {
        private UpdateAction()
        {
            super("Update", UPDATE_ICON);
        }

        public void actionPerformed(ActionEvent e)
        {
            trySave();
            UpdateResult result = svnFacade.update(svnFiles());
            if(result.isConflict() && result.isConflictsHandled()) {
                SwingUtils.warnUser(appContainer.getMainFrame(), "There were conflicts, but they were automatically resolved");
            }
            else if (result.isConflict())
            {
                SwingUtils.warnUser(appContainer.getMainFrame(), "You have a conflict. You should close the application and fix it manually\n" +
                        "You can revert the file, but then you will lose your changes");
                System.out.println(result);
            }
            else
            {
                reloadFile();
            }
        }
    }

    public void reloadFile() {
        appContainer.disposeAndReload();
    }

    public String getCurrentUser()
    {
        return svnFacade !=null ? svnFacade.getUsername() : "";
    }

    public SVNFacade getSvnFacade() {
        return svnFacade;
    }

    private File currentFile()
    {
        return appContainer.getGuiContext().getCurrentFile();
    }

    private class ExitCommitHook implements ApplicationContainer.Hook
    {
        public void execute()
        {
            trySave();
            if(svnFacade.hasLocalChanges(currentFile())) {
                int i = JOptionPane.showOptionDialog(appContainer.getMainFrame(),
                        "Would you like to commit your changes?", "SVN Commit",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (i == JOptionPane.YES_OPTION)
                {
                    String commitMessage = getCommitMessage();
                    if (commitMessage != null) {
                        commit(commitMessage);
                    }
                }
            }
            else {
                //SwingUtils.warnUser(m_appContainer.getMainFrame(), "No Changes to Commit");
            }
        }

    }

    protected abstract void trySave();
    protected java.util.List<File> extraSvnFiles() {
        return new ArrayList<File>();
    }

    private void commit(String message)
    {
        trySave();
        try
        {
            boolean updated = svnFacade.commit(message, svnFiles());
            if(updated) {
                System.out.println("reloading xml file because of update");
                appContainer.disposeAndReload();
            }
        }
        catch (RuntimeException e)
        {
            if (e.getCause() instanceof SVNException)
            {
                SVNException svnException = (SVNException) e.getCause();
                SwingUtils.warnUser(appContainer.getMainFrame(), svnException.getMessage());
            }
            else
            {
                throw e;
            }
        }
    }

    private File[] svnFiles() {
        List<File> files = extraSvnFiles();
        files.add(currentFile());
        return files.toArray(new File[files.size()]);
    }
}
