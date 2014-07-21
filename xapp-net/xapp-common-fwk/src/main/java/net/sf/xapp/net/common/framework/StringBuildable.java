/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import java.io.Serializable;
import java.util.List;

/**
 * A StringBuildable is coupled to implementation of the
 * {@link StringSerializable} interface.
 */
public interface StringBuildable extends Serializable
{
    void writeString(StringBuilder sb);
    void populateFrom(List<Object> data);
}