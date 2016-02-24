package net.sf.xapp.utils;

/**
 * Created by oldDave on 24/02/16.
 */
public class NoOpFilter<T> implements Filter<T> {
    @Override
    public boolean matches(T t) {
        return true;
    }
}
