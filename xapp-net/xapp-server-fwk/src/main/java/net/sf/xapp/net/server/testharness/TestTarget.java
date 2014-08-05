/*
 *
 * Date: 2010-dec-01
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.testharness;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.framework.TransportObject;

public interface TestTarget extends MessageHandler
{
    TransportObject getTarget();

    TestCase getTestCase();

}
