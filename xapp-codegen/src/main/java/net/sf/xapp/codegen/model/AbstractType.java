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

public abstract class AbstractType extends AbstractArtifact implements Type, Artifact {
    private boolean skipGeneration;
    private String alternativePackageName;

    public AbstractType(String name) {
        super(name);
    }

    public AbstractType() {
    }

    @Transient
    public String getPackageName() {
        return alternativePackageName != null ? alternativePackageName : packageName();
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

    public boolean isSkipGeneration() {
        return skipGeneration;
    }

    public void setSkipGeneration(boolean skipGeneration) {
        this.skipGeneration = skipGeneration;
    }

    public boolean shouldGenerate() {
        return isChangedInSession() && !isSkipGeneration();
    }
}