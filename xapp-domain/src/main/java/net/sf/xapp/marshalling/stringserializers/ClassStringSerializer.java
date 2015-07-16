package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.utils.ReflectionUtils;

/**
 * Created by oldDave on 16/07/2015.
 */
public class ClassStringSerializer implements StringSerializer<Class> {
    @Override
    public Class read(String str) {
        return str != null ? ReflectionUtils.classForName(str) : null;
    }

    @Override
    public String write(Class obj) {
        return obj!=null ?  obj.getName() : null;
    }

    @Override
    public String validate(String text) {
        try {
            Class.forName(text);
            return null;
        }
        catch (ClassNotFoundException e) {
            return e.getMessage();
        }
    }
}
