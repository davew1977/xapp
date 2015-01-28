package net.sf.xapp.application.editor;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.application.api.ObjectWidget;
import net.sf.xapp.application.api.PropertyWidget;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.EditorWidgetFactory;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.utils.XappException;

/**
 */
public class EditorUtils {


    public static PropertyWidget createBoundProperty(ClassModel classModel, Property property, Editor defaultEditor)
    {
        EditorWidget ew = classModel.getEditorWidget();
        return ew != null ? (PropertyWidget) new EditorWidgetFactory(ew).create(property, defaultEditor) : null;
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
