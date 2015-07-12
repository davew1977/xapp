package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/9/14
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class NamespacePath extends LinkedList<ObjectMeta> {
    public static final String PATH_SEPARATOR = "/";

    public String getPathFrom(Namespace namespace) {
        return getPathFrom(namespace, PATH_SEPARATOR);
    }

    public String getPathFrom(Namespace namespace, String pathSeparator) {
        return StringUtils.join(subList(indexOf(namespace) + 1, size()), pathSeparator);
    }

    public <T> List<T> instancePath(Class<T> filter) {
        return ObjectMeta.toInstances(filter, this);
    }

    @Override
    public String toString() {
        return asString();
    }

    public String asString() {
        return asString(PATH_SEPARATOR);
    }

    public String asString(String pathSeparator) {
        return getPathFrom(getFirst(), pathSeparator);
    }

    public static String fullPath(Namespace namespace, ObjectMeta obj) {
        return fullPath(namespace, obj, PATH_SEPARATOR);
    }

    public static String fullPath(Namespace namespace, ObjectMeta obj, String pathSeparator) {
        NamespacePath path = obj.getPath();
        String pathString = path.getPathFrom(namespace, pathSeparator);
        String key = obj.getKey();
        return !pathString.isEmpty() ? pathString + pathSeparator +key : key;
    }
}
