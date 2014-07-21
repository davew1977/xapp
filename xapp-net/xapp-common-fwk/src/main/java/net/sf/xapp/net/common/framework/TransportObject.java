/*
 *
 * Date: 2010-jun-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import java.io.Serializable;

public interface TransportObject extends Serializable,
        com.hazelcast.nio.DataSerializable, StringSerializable, StringBuildable, PrettyPrinter
{
    ObjectType type();
}
