/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.PropertyOrder;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArtifact extends FileMeta implements Artifact {
    private String description;
    private Module module;
    protected boolean changedInSession;

    public AbstractArtifact(String name) {
        super(name);
    }

    public AbstractArtifact() {
    }

    @Transient
    public String getPackageName() {
        return packageName();
    }

    @EditorWidget(FreeTextPropertyWidget.class)
    @PropertyOrder(1)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getName();
    }

    public AbstractArtifact clone() throws CloneNotSupportedException {
        return (AbstractArtifact) super.clone();
    }

    public List<String> validate() {
        return new ArrayList<String>();
    }

    @Transient
    public boolean isChangedInSession() {
        return changedInSession;
    }

    public void setChangedInSession(boolean changedInSession) {
        this.changedInSession = changedInSession;
    }

    @Override
    public void setModule(Module module) {
        this.module = module;
    }

    @Transient
    public Module getModule() {
        return module != null ? module : module();
    }

    public String className() {
        return getPackageName() + "." + getName();
    }

    public Model model() {
        return getModule().model();
    }
}