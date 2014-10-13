package net.sf.xapp.objectmodelling.core;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public interface Stepper<T> {
    void step(T item, int index);
}
