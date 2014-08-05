/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

public interface MultiNodeClientGUIListener<T>
{
    void newMessageTyped(T src, String message);
}