/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import ngpoker.common.types.ApiType;

public interface InMessage<I,V> extends Message
{
    Class<I> api();

    ApiType apiType();
    /**
     * @return the key of the subject of the request, or null if n/a
     */
    String entityKey();

    String messageType();

    /**
     * Call the appropriate handle method on the incoming api
     * @param in
     * @return
     */
    V visit(I in);

    Object principal();

    /**
     * is the message so critical that it must be saved if undelivered?
     * @return
     */
    boolean isPersistent();
}
