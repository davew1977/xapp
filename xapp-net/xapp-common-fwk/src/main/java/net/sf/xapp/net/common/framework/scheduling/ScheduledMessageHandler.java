/*
 *
 * Date: 2010-nov-03
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.scheduling;

import net.sf.xapp.net.common.framework.InMessage;

public interface ScheduledMessageHandler<A>
{
    void init(A api);
    Task invokeLater(InMessage<A,Void> message, long delay);
}
