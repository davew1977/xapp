/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.PropertyOrder;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArtifact extends FileMeta implements Artifact {
    private String description;

    public AbstractArtifact(String name) {
        super(name);
    }

    public AbstractArtifact() {
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

}