package net.sf.xapp.objectmodelling.core;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class ContainerAdd extends AbstractPropertyChange {
    public final Object result;

    public ContainerAdd(Property property, Object target, Object result) {
        super(property, target);
        this.result = result;
    }

    public boolean wasAdded() {
        return property instanceof ListProperty ? (Boolean) result : true;
    }

    @Override
    public boolean succeeded() {
        return wasAdded();
    }
}
