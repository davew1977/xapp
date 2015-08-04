package net.sf.xapp.objectmodelling.core;

import java.util.Objects;

/**
 * Created by oldDave on 03/08/2015.
 */
public class ObjKey {
    private final Object obj;

    public ObjKey(Object obj) {
        this.obj = obj;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ObjKey && ((ObjKey) o).obj == obj;
    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }
}
