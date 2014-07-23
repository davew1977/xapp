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
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractType extends FileMeta implements Type, Artifact {
    private String description;
    private boolean skipGeneration;
    private String alternativePackageName;
    private Module module;
    private boolean changedInSession;

    public AbstractType(String name) {
        super(name);
    }

    public AbstractType() {
    }

    @Transient
    public String getPackageName() {
        return alternativePackageName != null ? alternativePackageName : packageName();
    }

    @EditorWidget(FreeTextPropertyWidget.class)
    @PropertyOrder(1)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setPackageName(String packageName) {
        this.alternativePackageName = packageName;
    }

    public String getAlternativePackageName() {
        return alternativePackageName;
    }

    public void setAlternativePackageName(String alternativePackageName) {
        this.alternativePackageName = alternativePackageName;
    }

    public String toString() {
        return getName();
    }

    public AbstractType clone() throws CloneNotSupportedException {
        return (AbstractType) super.clone();
    }

    public List<String> validate() {
        return new ArrayList<String>();
    }

    public boolean isSkipGeneration() {
        return skipGeneration;
    }

    public void setSkipGeneration(boolean skipGeneration) {
        this.skipGeneration = skipGeneration;
    }

    public boolean shouldGenerate() {
        return changedInSession && !isSkipGeneration();
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