package net.sf.xapp.codegen.plugin;

import net.sf.xapp.application.utils.codegen.AbstractCodeFile;
import net.sf.xapp.codegen.Generator;
import net.sf.xapp.utils.XappLoggerAdaptor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import net.sf.xapp.codegen.model.Model;
import net.sf.xapp.codegen.model.Module;

import java.io.File;
import java.util.Arrays;

/**
 */
@Mojo(name="generate", defaultPhase= LifecyclePhase.GENERATE_SOURCES)
public class CodegenMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Parameter(property = "generate.modelFilePath", defaultValue = "${project.parent.basedir}/domain-model.xml")
    private String modelFilePath;
    /**
     * target directory for generated sources
     *
     * default assumes a parent maven project as the basedir
     */
    @Parameter(property = "generate.baseDir", defaultValue = "${project.parent.basedir}" )
    private File baseDir;

    @Parameter(property = "generate.moduleName", defaultValue = "${project.artifactId}")
    private String moduleName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty("model.file.path", modelFilePath);
        Generator generator = new Generator();
        Model model = generator.loadModel();
        Module module = model.cdb.getInstance(Module.class, moduleName);
        File targetDir = new File(baseDir, module.getOutDir());
        this.project.addCompileSourceRoot(targetDir.getAbsolutePath());
        if(targetDir.exists()) {
            getLog().info("Skipping generation, not building clean");
            return;
        }

        AbstractCodeFile.logger = new MyXappLogger();
        getLog().info(modelFilePath);
        getLog().info(targetDir.getAbsolutePath());
        generator.getGeneratorContext().setBaseDir(baseDir);
        generator.generateAndWrite(model, Arrays.asList(module), true);
    }

    private class MyXappLogger extends XappLoggerAdaptor {
        @Override
        public void info(String message) {
            getLog().info(message);
        }
    }
}
