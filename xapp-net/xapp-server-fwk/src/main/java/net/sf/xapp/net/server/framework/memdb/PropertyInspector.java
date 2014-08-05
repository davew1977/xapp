/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.memdb;

public interface PropertyInspector<T>
{
    String getValue(T item, String property);
}
