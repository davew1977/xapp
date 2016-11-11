/*
 *
 * Date: 2010-jun-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public interface TransportObject extends Serializable,
        com.hazelcast.nio.serialization.DataSerializable, StringSerializable, StringBuildable, PrettyPrinter
{
    ObjectType type();

    /*
    following added as part of hazelcast upgrade, which uses its own DataInput/Output extensions
     */
    void readData(DataInput in) throws IOException;
    void writeData(DataOutput out) throws IOException;
}
