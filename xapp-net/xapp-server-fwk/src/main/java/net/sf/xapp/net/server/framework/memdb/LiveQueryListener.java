/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.memdb;


import ngpoker.common.types.ListOp;

public interface LiveQueryListener<T>
{
    void itemAdded(T item);
    void itemRemoved(T item);

    void itemChanged(T item, String propName, String value);
    void itemChanged(T item, String propName, int index, String value,  ListOp listOp);
}
