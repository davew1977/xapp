/*
 *
 * Date: 2010-jun-02
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public interface DataSerializable extends Serializable
{
    void writeData(DataOutput out) throws IOException;

    void readData(DataInput in) throws IOException;
}
