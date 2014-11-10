package net.sf.xapp.utils;

/**
 * Created with IntelliJ IDEA.
 * User: oldDave
 * Date: 09/11/2014
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public interface Grouping<K,V> {
    K getGroup(V t);
}
