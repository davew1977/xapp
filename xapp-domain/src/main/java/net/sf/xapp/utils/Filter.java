package net.sf.xapp.utils;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public interface Filter<T> {
    boolean matches(T t);
}
