package net.sf.xapp.objectmodelling.core;

import net.sf.xapp.utils.StringUtils;

import java.util.LinkedList;

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

    @Override
    public String toString() {
        return getPathFrom(getFirst());
    }

    public static String fullPath(Namespace namespace, ObjectMeta obj) {
        NamespacePath path = obj.getPath();
        String pathString = path.getPathFrom(namespace);
        String key = obj.getKey();
        return !pathString.isEmpty() ? pathString + PATH_SEPARATOR+key : key;
    }
}
