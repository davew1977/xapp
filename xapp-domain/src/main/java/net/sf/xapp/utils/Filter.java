package net.sf.xapp.utils;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public interface Filter<T> {
    boolean matches(T t);
}
