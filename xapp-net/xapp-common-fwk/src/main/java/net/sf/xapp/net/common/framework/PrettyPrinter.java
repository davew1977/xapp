/*
 *
 * Date: 2010-feb-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

public interface PrettyPrinter
{
    String expandToString();
    void expandToString(StringBuilder sb, String indent);
}