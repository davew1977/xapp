/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

/**
 * major concept in an application - typically an api or a type
 */
public interface Artifact
{
    String getName();
    boolean isChangedInSession();
    void setChangedInSession(boolean changed);
    Module getModule();
    void setModule(Module module);
    String getPackageName();
}
