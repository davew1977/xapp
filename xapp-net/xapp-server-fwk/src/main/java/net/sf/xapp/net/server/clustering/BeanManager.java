/*
 *
 * Date: 2010-aug-31
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.common.framework.MessageHandler;

public interface BeanManager
{
    <A> MessageHandler<A> findBean(Class<A> apiType);
}
