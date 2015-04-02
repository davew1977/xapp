/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.objectmodelling.Transient;

import java.util.List;

public abstract class AbstractType extends AbstractArtifact implements Type, Artifact {

    public AbstractType(String name) {
        super(name);
    }

    public AbstractType() {
    }

}