/*
 *
 * Date: 2010-aug-11
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import ngpoker.common.types.ListOp;

public interface Storable
{
    String getKey();
    String get(String propName);
    void set(String propName, String value);
    void set(String propName, int index, String value, ListOp listOp);
}
