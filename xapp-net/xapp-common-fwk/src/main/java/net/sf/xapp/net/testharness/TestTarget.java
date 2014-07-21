/*
 *
 * Date: 2010-dec-01
 * Author: davidw
 *
 */
package net.sf.xapp.net.testharness;

import net.sf.xapp.net.common.framework.MessageHandler;
import net.sf.xapp.net.common.framework.TransportObject;

public interface TestTarget extends MessageHandler
{
    TransportObject getTarget();

    TestCase getTestCase();

}
