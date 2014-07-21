/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

public interface OutMessage<T>
{
    String expandToString();

    String serialize();

    /**
     *
     * @param out the out bound api. Visitor allows for dynamic dispatch - aspects without spring reflection magic
     */
    void visit(T out);
}