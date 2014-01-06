/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 */
package net.sf.xapp.application.diff;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.difftracking.DiffSet;
import net.sf.xapp.application.utils.SwingUtils;

public class WizardLauncher
{
    public static void main(String[] args)
    {
        Unmarshaller un = new Unmarshaller(DiffSet.class);
        DiffSet baseToMine = (DiffSet) un.unmarshal(WizardLauncher.class.getResourceAsStream("BaseToMine.xml"));
        DiffSet baseToTheirs = (DiffSet) un.unmarshal(WizardLauncher.class.getResourceAsStream("BaseToTheirs.xml"));
        DiffSet mineToTheirs = (DiffSet) un.unmarshal(WizardLauncher.class.getResourceAsStream("MineToTheirs.xml"));

        DiffModel diffModel = new DiffModel(baseToMine, baseToTheirs, mineToTheirs);

        DiffWizardViewImpl view = new DiffWizardViewImpl();
        DiffWizardPresenter wizard = new DiffWizardPresenterImpl(view);
        wizard.init(diffModel);

        SwingUtils.showInFrame(view.getMainContainer());
    }
}
