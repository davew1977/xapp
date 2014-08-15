/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.smartconverter;

import net.sf.xapp.codegen.model.ComplexType;
import net.sf.xapp.net.common.framework.LispObj;
import net.sf.xapp.net.server.framework.persistendb.FileContentConverter;

public interface Converter extends FileContentConverter
{
    /**
     * do structural changes to the given string to convert it to the given type
     *
     * don't delve into the fields and sub types.
     *
     * The returned string should be a lispobj whose field count corresponds to the type
     * and whose fields match as follows:
     *      complex types and lists should return "[]"
     *      primitive types and enums can return "" if the field is optional, or a suitable
     *                                   default otherwise
     *
     */
    ConvertResult<LispObj> convert(LispObj obj, ComplexType type);

    String getSourceVersion();

    String getTargetVersion();

}
