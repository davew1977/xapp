package net.sf.xapp.application.editor;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.application.api.ObjectWidget;
import net.sf.xapp.application.api.PropertyWidget;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.utils.XappException;

import java.lang.reflect.Constructor;

/**
 */
public class EditorUtils {


    public static PropertyWidget createBoundProperty(ClassModel classModel, Property property, Editor defaultEditor)
    {
        if (classModel.getEditorWidget() == null) return null;
        try
        {
            Class boundPropertyClass = getBoundPropertyClass(classModel.getEditorWidget());
            Constructor constructor = boundPropertyClass.getConstructor(Property.class, DefaultEditor.class);
            Object o = constructor.newInstance(property, defaultEditor);
            return (PropertyWidget) o;
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    public static ObjectWidget createBoundObject(ClassModel classModel)
    {
        if (classModel.getBoundObjectType() == null) return null;
        try
        {
            ObjectWidget objectWidget = (ObjectWidget) classModel.getBoundObjectType().value().newInstance();
            ClassModel.tryAndInject(objectWidget, classModel.getClassDatabase(), "m_classDatabase");
            objectWidget.init();
            return objectWidget;
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }


    private static Class getBoundPropertyClass(EditorWidget bcAnnotation) {

        return bcAnnotation!=null ? resolveClass(bcAnnotation.value(), bcAnnotation.className()) : null;
    }

    /**
     * @return aClass unless object, otherwise, will try and find class specified by className
     */
    private static Class resolveClass(Class aClass, String className) {
        if(aClass.equals(Object.class)) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return aClass;
    }


    public static PropertyWidget createBoundProperty(Property property)
    {
        return createBoundProperty(getBoundPropertyClass(property.getEditorWidget()));
    }

    public static PropertyWidget createBoundProperty(Class boundPropType)
    {
        try
        {
            return (PropertyWidget) boundPropType.newInstance();
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }
}
