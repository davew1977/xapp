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
package net.sf.xapp.application.diff;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.application.utils.html.HTML;
import net.sf.xapp.application.utils.html.HTMLImpl;
import net.sf.xapp.objectmodelling.difftracking.Conflict;
import net.sf.xapp.objectmodelling.difftracking.ConflictType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

public class DiffWizardViewImpl implements DiffWizardView
{
    private JTabbedPane m_tabbedPane;
    private DiffSummary m_diffSummary;
    private DiffBrowserSet m_diffBrowserSet;
    private DiffWizardPresenter m_presenter;
    private Object m_conflictBrowser = new Object();


    public DiffWizardViewImpl()
    {
        m_diffSummary = new DiffSummary();
        m_diffBrowserSet = new DiffBrowserSet();
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.addTab("Summary", m_diffSummary);
        SwingUtils.setFont(m_tabbedPane, SwingUtils.DEFAULT_FONT);

        m_tabbedPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if(e.getSource().equals(m_diffSummary))
                {
                    m_presenter.setViewState(DiffWizardState.SUMMARY);
                }
                else if(e.getSource().equals(m_diffBrowserSet))
                {
                    m_presenter.setViewState(DiffWizardState.BROWSE_DIFFS);
                }
                else if(e.getSource().equals(m_conflictBrowser))
                {
                    m_presenter.setViewState(DiffWizardState.BROWSE_CONFLICTS);
                }
            }
        });
    }

    public void setPresenter(DiffWizardPresenter presenter)
    {
        m_presenter = presenter;
        m_diffSummary.setPresenter(presenter);
        m_diffBrowserSet.setPresenter(presenter);
    }

    public void showSummary(SummaryBean summary)
    {
        HTML html = new HTMLImpl();
        html.bean(summary);
        html.b().table().tr("Changes", "", "").b();
        html.color(Color.BLUE).tr("New Objects:", "${newObjects}");
        html.color(Color.GREEN).tr("Removed Objects:", "${removedObjects}");
        html.color(Color.RED).tr("Property Changes:", "${propertyChanges}");

        if (!summary.m_conflicts.isEmpty())
        {
            html.p("CONFLICTS EXIST");
        }

        m_diffSummary.setContent(html.html());
    }

    public void showDiffs(DiffsBean diffs)
    {
        if(m_tabbedPane.indexOfComponent(m_diffBrowserSet)==-1)
        {
            m_tabbedPane.addTab("Diff Browser", m_diffBrowserSet);
        }
        m_diffBrowserSet.showDiffs(diffs);
        m_tabbedPane.setSelectedComponent(m_diffBrowserSet);
    }

    public void setBrowseButtonsEnabled(boolean previous, boolean next, boolean first, boolean last)
    {
        m_diffBrowserSet.setBrowseButtonsEnabled(previous, next, first, last);

    }

    public static void main(String[] args)
    {
        DiffWizardViewImpl view = new DiffWizardViewImpl();
        SummaryBean summaryBean = new SummaryBean();
        summaryBean.m_conflicts = new ArrayList<Conflict>();
        summaryBean.m_conflicts.add(new Conflict(null,null, ConflictType.COMPLEX_DIFF_DIFF));
        view.showSummary(summaryBean);
        SwingUtils.showInFrame(view.m_tabbedPane);

    }

    public Container getMainContainer()
    {
        return m_tabbedPane;
    }
}
