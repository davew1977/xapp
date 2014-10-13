package net.sf.xapp.application.strategies;

import javax.swing.*;
import java.io.File;

import net.sf.xapp.application.api.GUIContext;
import net.sf.xapp.application.core.ApplicationContainerImpl;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class StandaloneSaveStrategy implements SaveStrategy {

    private final ApplicationContainerImpl appContainer;

    public StandaloneSaveStrategy(ApplicationContainerImpl appContainer) {
        this.appContainer = appContainer;
    }

    @Override
    public void save() {
        GUIContext guiContext = appContainer.getGuiContext();
        if (guiContext.getCurrentFile() == null)
        {

            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            int returnVal = chooser.showSaveDialog(appContainer.getMainPanel());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                guiContext.saveToFile(chooser.getSelectedFile());
            }
        }
        else
        {
            guiContext.saveToFile();
        }
        appContainer.updateFrameTitle();
    }
}
