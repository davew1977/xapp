package net.sf.xapp.objectmodelling.core;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface Stepper<T> {
    void step(T item, int index);
}
