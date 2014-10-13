package net.sf.xapp.net.common.framework;


import net.sf.xapp.net.common.framework.Entity;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Ref<T extends Entity> {
    private Object key;
    private Class<T> type;
    private Lookup lookup;

    public Ref(Class<T> type, Object key) {
        this.type = type;
        this.key = key;
    }

    public Ref(Class<T> type, T value) {
        this(type, value.getKey());
    }

    public void init(Lookup lookup){
        this.lookup = lookup;
    }

    public String getKey() {
        return (String) key;
    }

    public T get() {
        return lookup != null ? lookup.lookup(type, (String) key) : null;
    }

    public void set(T value) {
        key = value.getKey();
    }

    @Override
    public String toString() {
        return (String) key;
    }
}
