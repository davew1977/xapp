/*
 *
 * Date: 2010-sep-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import ngpoker.infrastructure.types.NodeId;

public interface ServiceLookup
{
    NodeId lookupService(Class api);
}
