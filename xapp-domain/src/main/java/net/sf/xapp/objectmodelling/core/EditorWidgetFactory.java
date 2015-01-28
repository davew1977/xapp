package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.utils.ReflectionUtils;

/**
 *
 */
public class EditorWidgetFactory {
    private Class type;
    private String args;

    public EditorWidgetFactory(EditorWidget editorWidget) {
        if(editorWidget.value() != null) {
            type = editorWidget.value();
        } else {
            type = ReflectionUtils.classForName(editorWidget.className());
        }
        args = editorWidget.args();
    }

    public EditorWidgetFactory(Class type, Object... args) {
        this.type = type;
        if(args.length > 0 && args[0] instanceof String) {
            this.args = (String) args[0];
        }
    }

    public EditorWidgetFactory(String className) {
        this.type = ReflectionUtils.classForName(className);
    }

    public Object create(Object... args) {
        return ReflectionUtils.newInstance(type, args);

    }

    public String args() {
        return args;
    }
}
