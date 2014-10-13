package net.sf.xapp.objectmodelling.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class ObjectMetaMap {
    private List<Map<String, ObjectMeta>> maps = new ArrayList<Map<String, ObjectMeta>>();

    public void add(Map<String, ObjectMeta> map) {
        maps.add(map);
    }

    public ObjectMeta find(String key) {
        for (Map<String, ObjectMeta> map : maps) {
            ObjectMeta objectMeta = map.get(key);
            if(objectMeta != null) {
                return objectMeta;
            }
        }
        return null;
    }
}
