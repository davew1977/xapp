/*
 *
 * Date: 2010-jun-21
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

public interface MessageHandler<A>
{
    <T> T handleMessage(InMessage<A, T> inMessage);
}