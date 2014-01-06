/*
 *
 * Date: 2010-jun-09
 * Author: davidw
 *
 */
package net.sf.xapp.application.utils.codegen;

/**
 * Enum values can be a bit like static inner classes
 */
public interface EnumContext
{
    CodeFile _final();
    CodeFile _static();
    CodeFile endBlock();
    CodeFile startBlock(String code, Object... args);
    CodeFile line(String code, Object... args);
    CodeFile method(String name, String returnType, String... params);
}
