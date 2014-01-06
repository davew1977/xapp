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

public interface DiffWizardPresenter
{
    void browseAll();
    void browseConflicts();
    void acceptNonConflicts();
    void resolveConflict(Conflict conflict, DiffToAccept diffToAccept);
    void acceptAll();
    void init(DiffModel model);
    void previous();
    void last();
    void next();
    void first();
    void setSelectedDiffType(DiffType diffType);
    void setViewState(DiffWizardState browseConflicts);
}
