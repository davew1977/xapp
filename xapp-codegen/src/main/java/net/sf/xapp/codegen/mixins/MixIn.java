/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.mixins;

import net.sf.xapp.application.utils.codegen.CodeFile;

/**
 * mixes in behaviour into the generated code
 */
public interface MixIn<T>
{
    void mixIn(T obj, CodeFile ct);
}