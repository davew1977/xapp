/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.PropertyOrder;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractType implements Type, Artifact
{
    private String m_name;
    private String m_description;
    private boolean m_skipGeneration;
    private String m_alternativePackage;
    private String packageName;
    private boolean changedInSession;
    private Module module;

    public AbstractType(String name)
    {
        m_name = name;
    }

    public AbstractType()
    {
    }

    @Transient
    public String getPackageName() {
        return packageName;
    }

    @Key
    @PropertyOrder(-1)
    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    @EditorWidget(FreeTextPropertyWidget.class)
    @PropertyOrder(1)
    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String description)
    {
        m_description = description;
    }

    public String getAlternativePackage()
    {
        return m_alternativePackage;
    }

    @Override
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;

    }

    @Override
    public String derivePackage()
    {
        if(m_alternativePackage!=null)
        {
            return m_alternativePackage;
        }
        return packageName;
    }

    public void setAlternativePackage(String alternativePackage)
    {
        m_alternativePackage = alternativePackage;
    }

    public String toString()
    {
        return getName();
    }

    public AbstractType clone() throws CloneNotSupportedException
    {
        return (AbstractType) super.clone();
    }

    public List<String> validate()
    {
        return new ArrayList<String>();
    }

    public boolean isSkipGeneration()
    {
        return m_skipGeneration;
    }

    public void setSkipGeneration(boolean skipGeneration)
    {
        m_skipGeneration = skipGeneration;
    }

    public boolean shouldGenerate()
    {
        return changedInSession && !isSkipGeneration();
    }

    @Transient
    public boolean isChangedInSession()
    {
        return changedInSession;
    }

    public void setChangedInSession(boolean changedInSession)
    {
        this.changedInSession = changedInSession;
    }

    @Override
    public void setModule(Module module) {
        this.module = module;
    }

    @Transient
    public Module getModule() {
        return module;
    }

    public String className()
    {
        return derivePackage() + "." + getName();
    }

    public Model model() {
        return getModule().model();
    }
}