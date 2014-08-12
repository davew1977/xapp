/*
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.InMessage;

/**
 *
 */
public interface QHandler extends Runnable
{

    /**
     * Blocking method that will wait for the next message and pass it into the core
     * Also is responsible for rejecting messages if this node is shutting down.
     */

    void processMessage(InMessage inMessage);
}