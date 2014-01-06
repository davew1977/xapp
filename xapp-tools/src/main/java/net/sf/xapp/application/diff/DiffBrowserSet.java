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
import net.sf.xapp.objectmodelling.difftracking.Diff;
import net.sf.xapp.objectmodelling.difftracking.PropertyDiff;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DiffBrowserSet extends JTabbedPane
{
    private DiffBrowser m_simple;
    private DiffBrowser m_added;
    private DiffBrowser m_removed;
    private DiffBrowser m_complex;
    private DiffBrowser m_refList;
    private DiffWizardPresenter m_presenter;

    public DiffBrowserSet()
    {
        m_simple = new DiffBrowser(DiffType.SIMPLE);
        m_added = new DiffBrowser(DiffType.OBJECT_ADDED);
        m_removed = new DiffBrowser(DiffType.OBJECT_REMOVED);
        m_complex = new DiffBrowser(DiffType.COMPLEX);
        m_refList = new DiffBrowser(DiffType.REF_LIST);
        addTab("added objects", m_added);
        addTab("simple diffs", m_simple);
        addTab("removed objects", m_removed);
        addTab("reference list", m_refList);
        addTab("complex", m_complex);

        SwingUtils.setFont(this, SwingUtils.DEFAULT_FONT);

        addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if(e.getSource() instanceof DiffBrowser)
                {
                    m_presenter.setSelectedDiffType(((DiffBrowser) e.getSource()).m_diffType);
                }
            }
        });
    }

    public void showDiffs(DiffsBean diffs)
    {
        if (diffs == null) return;
        setSelectedComponent(getTab(diffs.m_selectedType));
        StringBuilder sb = new StringBuilder("<html><head>\n" +
                "  <title>  </title>\n" +
                "  <style type=\"text/css\">\n" +
                "  body {\n" +
                "    font: Dialog;\n" +
                "    font-size: 8;\n" +
                "    background-color: #ffffff }\n" +
                "  </style>\n" +
                "</head><body>");
        switch (diffs.m_selectedType)
        {

        case SIMPLE:
            DiffGroupBean diffGroup = diffs.m_diffGroupsByType.get(DiffType.SIMPLE);
            if(diffGroup.m_objectDiffs.isEmpty())
            {
                sb.append("NO SIMPLE DIFFS");
            }
            else
            {
                ObjectDiffBean objectDiff = diffGroup.m_objectDiffs.get(diffGroup.m_selectedIndex);

                PropertyDiff propertyDiff = (PropertyDiff) objectDiff.m_diffs.get(0);
                sb.append("<html><p>Changes in ").append(propertyDiff.getClazz()).append(":").append(propertyDiff.getKey()).append("</p>");
                sb.append("<table><tr><td>Property</td><td>Old Value</td><td>New Value</td></tr>");
                for (Diff diff : objectDiff.m_diffs)
                {
                    PropertyDiff pd = (PropertyDiff) diff;
                    sb.append("<tr><td>").append(pd.getProperty()).append("</td><td>").append(pd.getOldValue()).append("</td><td>").append(pd.getNewValue()).append("</td></tr>");
                }
                sb.append("</table>");
            }
            break;
        case OBJECT_ADDED:
            break;
        case OBJECT_REMOVED:
            break;
        case REF_LIST:
            break;
        case COMPLEX:
            break;
        }
        sb.append("</body></html>");

        ((DiffBrowser) getSelectedComponent()).setContent(sb.toString());
    }

    private Component getTab(DiffType selectedType)
    {
        switch (selectedType)
        {
        case COMPLEX: return m_complex;
        case OBJECT_ADDED: return m_added;
        case OBJECT_REMOVED: return m_removed;
        case REF_LIST: return m_refList;
        case SIMPLE: return m_simple;
        }
        return null;
    }

    public void setBrowseButtonsEnabled(boolean previous, boolean next, boolean first, boolean last)
    {
        ((DiffBrowser) getSelectedComponent()).m_firstButton.setEnabled(first);
        ((DiffBrowser) getSelectedComponent()).m_lastButton.setEnabled(last);
        ((DiffBrowser) getSelectedComponent()).m_previousButton.setEnabled(previous);
        ((DiffBrowser) getSelectedComponent()).m_nextButton.setEnabled(next);
    }

    public void setPresenter(DiffWizardPresenter presenter)
    {
        m_presenter = presenter;
        m_added.setPresenter(presenter);
        m_removed.setPresenter(presenter);
        m_complex.setPresenter(presenter);
        m_simple.setPresenter(presenter);
        m_refList.setPresenter(presenter);
    }
}
