/*
 *
 * Date: 2010-jun-03
 * Author: davidw
 *
 */
package net.sf.xapp.codegen;

import net.sf.xapp.application.utils.codegen.JavaFile;
import net.sf.xapp.codegen.model.Artifact;
import net.sf.xapp.codegen.model.Model;
import net.sf.xapp.codegen.model.Module;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeneratorContext
{
    private File overrideOutDir;
    private File baseDir = new File(".");
    public String modelFilePath;
    private boolean isLight;
    private List<Module> activeModules = new ArrayList<Module>();

    public GeneratorContext(boolean isLight)
    {
        this.isLight = isLight;
    }

    public void setActiveModules(List<Module> activeModules) {
        this.activeModules = activeModules;
    }

    public JavaFile createJavaFile(Artifact artifact) {
        return createJavaFile(artifact.getModule());
    }

    public JavaFile createJavaFile(Model model) {
        return createJavaFile(model.getBaseModule());
    }

    public JavaFile createJavaFile(Module module) {
        //todo handle generate for client case
        return new JavaFile(outDir(module), activeModules.contains(module));
    }

    private File outDir(Module module) {
        if(overrideOutDir != null) {
            return overrideOutDir;
        }
        return new File(baseDir, module.getOutDir());
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setOverrideOutDir(File overrideOutDir) {
        this.overrideOutDir = overrideOutDir;
    }

    public void setModelFilePath(String modelFilePath)
    {
        this.modelFilePath = modelFilePath;
    }

    /*public File getOutdir()
    {
        return outdir;
    }*/

    public String getModelFilePath()
    {
        return modelFilePath;
    }

    /**
     * hint whether or not to generate light, or less verbose code
     * unnecessary code can be omitted in "light" mode
     * @return
     */
    public boolean isLight()
    {
        return isLight;
    }
}
