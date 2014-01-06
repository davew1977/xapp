package net.sf.xapp.application.utils.codegen;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david.webber
 * Date: 2012-06-01
 * Time: 07:39
 * To change this template use File | Settings | File Templates.
 */
public interface ClassContext {
    CodeFile setName(String name);

    /**
     * set to be an interface, methods, will then be generated as stubs
     */
    CodeFile setInterface();

    /**
     * make the class abstract
     */
    CodeFile setAbstract();

    /**
     * Set the superclass of this class
     */
    CodeFile setSuper(String s);

    /**
     * Make this class static (only relevant for inner classes)
     */
    CodeFile setStatic();

    CodeFile addImplements(String i);

    CodeFile field(String type, String varname, Access access, String defaultValue);

    CodeFile field(String type, String varname, Access access);

    CodeFile field(String type, String varname, String defaultValue);

    CodeFile field(String type, String varname);

    CodeFile constructor(String... params);

    CodeFile defaultConstructor();

    CodeFile method(String name, String returnType, String... params);

    Method getAccessor(String fieldName);

    Method getModifier(String fieldName);

    Method getDefaultConstructor();

    Collection<Method> getMethods();

    List<Method> getConstructors();

    Method getMethod(String name, String... params);
    Field getField(String name);
    /**
     *  Not quite an overloading of "method" that also excepts a throwsClause
     */
    CodeFile method2(String name, String returnType, String throwsClause, String... params);

    CodeFile line(String code, Object... args);
    CodeFile setAutoSemiColon(boolean autoSemi);

    /**
     * blank line
     */
    CodeFile line();

    CodeFile startBlock(String code, Object... args);

    CodeFile endBlock();
    CodeFile endBlock(String code);

    /**
     * Adds a line of documentation to the current context, class, constructor, field or method
     * @param line
     * @return
     */
    CodeFile docLine(String line, String... args);

    /**
     * Annotate the next
     * @param line the annotation code to insert
     * @param args args to substitute into the line {@link String#format(String, Object[])}
     * @return
     */
    CodeFile annotate(String line, String... args);

    CodeFile param(String type, String varname);

    CodeFile param(String type, String varname, String doc);

    CodeFile iterate(String varname, boolean needIndex);

    /**
     * will make the next method or field be declared static
     * @return
     */
    CodeFile _static();

    /**
     * will make the next method or field be declared final, or, "const" in AS
     * @return
     */
    CodeFile _final();

    CodeFile _transient();

    CodeFile _abstract();

    /**
     * will modify the next field or method's access to "protected"
     * @return
     */
    CodeFile _protected();
    CodeFile _private();

    void clearFields();

    void clearConstructors();

    String getName();

    int getCurrentIndent();
}
