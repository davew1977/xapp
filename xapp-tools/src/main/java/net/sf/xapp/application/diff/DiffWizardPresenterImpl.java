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

import net.sf.xapp.objectmodelling.difftracking.Conflict;
import net.sf.xapp.objectmodelling.difftracking.Diff;
import net.sf.xapp.objectmodelling.difftracking.DiffSet;
import net.sf.xapp.utils.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiffWizardPresenterImpl implements DiffWizardPresenter
{
    private DiffWizardView m_view;
    private PresenterState m_state;
    private PresenterState SUMMARY;
    private PresenterState BROWSE_CONFLICTS;
    private PresenterState BROWSE_DIFFS;
    private DiffModel m_diffModel;
    private DiffsBean m_diffsBean;

    public DiffWizardPresenterImpl(DiffWizardView view)
    {
        m_view = view;
        m_view.setPresenter(this);
        SUMMARY = new Summary();
        BROWSE_DIFFS= new BrowseDiffs();
    }

    private void setState(PresenterState state)
    {
        m_state = state;
        m_state.enterState();
    }

    public void init(DiffModel diffModel)
    {
        m_diffModel = diffModel;
        setState(SUMMARY);
    }

    public void browseAll(){m_state.browseAll();}
    public void browseConflicts(){m_state.browseConflicts();}
    public void acceptAll(){m_state.acceptAll();}
    public void acceptNonConflicts(){m_state.acceptNonConflicts();}
    public void resolveConflict(Conflict conflict, DiffToAccept diffToAccept){m_state.resolveConflict(conflict, diffToAccept);}

    public void previous(){m_state.previous();}
    public void last(){m_state.last();}
    public void next(){m_state.next();}
    public void first(){m_state.first();}

    public void setSelectedDiffType(DiffType diffType){m_state.setSelectedDiffType(diffType);}

    public void setViewState(DiffWizardState wizardState)
    {
        switch (wizardState)
        {

        case BROWSE_CONFLICTS:setState(BROWSE_CONFLICTS);
            break;
        case BROWSE_DIFFS: setState(BROWSE_DIFFS);
            break;
        case SUMMARY: setState(SUMMARY);
            break;
        }

    }

    private DiffGroupBean createDiffGroup(List<? extends Diff> propDiffs)
    {
        HashMap<String, List<Diff>> map = new HashMap<String, List<Diff>>();
        for (Diff diff : propDiffs)
        {
            String key = diff.createKey();
            List<Diff> diffs = map.get(key);
            if(diffs==null)
            {
                diffs = new ArrayList<Diff>();
                map.put(key, diffs);
            }
            diffs.add(diff);
        }
        DiffGroupBean diffGroup = new DiffGroupBean();
        diffGroup.m_objectDiffs = new ArrayList<ObjectDiffBean>();
        for (List<Diff> diffs : map.values())
        {
            ObjectDiffBean diffBean = new ObjectDiffBean();
            diffBean.m_diffs = diffs;
            diffGroup.m_objectDiffs.add(diffBean);
        }
        return diffGroup;
    }

    public void enterState(){}

    private class PresenterState implements DiffWizardPresenter, State
    {
        public void browseAll(){}
        public void browseConflicts(){}
        public void acceptNonConflicts(){}
        public void resolveConflict(Conflict conflict, DiffToAccept diffToAccept){}
        public void enterState(){}
        public void acceptAll(){}
        public void init(DiffModel model){}
        public void previous(){}
        public void last(){}
        public void next(){}
        public void first(){}
        public void setSelectedDiffType(DiffType diffType){
            m_diffsBean.m_selectedType = diffType;
        }

        public void setViewState(DiffWizardState browseConflicts)
        {

        }
    }

    private class Summary extends PresenterState
    {
        @Override
        public void enterState()
        {
            m_view.showSummary(new SummaryBean(m_diffModel));
        }

        @Override
        public void browseAll()
        {
            setState(BROWSE_DIFFS);
        }
    }

    private class BrowseDiffs extends PresenterState
    {

        @Override
        public void enterState()
        {
            if(m_diffsBean==null)
            {
                m_diffsBean = new DiffsBean();
                DiffSet diffSet = m_diffModel.getMineToTheirs();
                m_diffsBean.m_diffGroupsByType.put(DiffType.SIMPLE, createDiffGroup(diffSet.getPropertyDiffs()));
                m_diffsBean.m_diffGroupsByType.put(DiffType.OBJECT_ADDED, createDiffGroup(diffSet.getNewNodeDiffs()));
                m_diffsBean.m_diffGroupsByType.put(DiffType.OBJECT_REMOVED, createDiffGroup(diffSet.getRemovedNodeDiffs()));
                m_diffsBean.m_diffGroupsByType.put(DiffType.REF_LIST, createDiffGroup(diffSet.getReferenceListDiffs()));
                m_diffsBean.m_diffGroupsByType.put(DiffType.COMPLEX, createDiffGroup(diffSet.getComplexPropertyDiffs()));
            }
            m_view.showDiffs(m_diffsBean);
            updateButtons();
        }

        @Override
        public void next()
        {
            getCurrentBean().m_selectedIndex++;
            m_view.showDiffs(m_diffsBean);
            updateButtons();
        }

        private void updateButtons()
        {
            int i = getCurrentBean().m_selectedIndex;
            int size = getCurrentBean().m_objectDiffs.size();
            m_view.setBrowseButtonsEnabled(i>0,i<size-1,i>0,i<size-1);
        }

        @Override
        public void previous()
        {
            getCurrentBean().m_selectedIndex--;
            m_view.showDiffs(m_diffsBean);
            updateButtons();
        }


        @Override
        public void last()
        {
            getCurrentBean().m_selectedIndex = getCurrentBean().m_objectDiffs.size()-1;
            m_view.showDiffs(m_diffsBean);
            updateButtons();
        }

        @Override
        public void first()
        {
            getCurrentBean().m_selectedIndex = 0;
            m_view.showDiffs(m_diffsBean);
            updateButtons();
        }

        private DiffGroupBean getCurrentBean()
        {
            return m_diffsBean.m_diffGroupsByType.get(m_diffsBean.m_selectedType);
        }
    }
}
