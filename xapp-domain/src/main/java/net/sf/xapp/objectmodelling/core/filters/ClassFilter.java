package net.sf.xapp.objectmodelling.core.filters;

import net.sf.xapp.utils.Filter;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class ClassFilter implements Filter {
    private final Class filterClass;

    public ClassFilter(Class filterClass) {
        this.filterClass = filterClass;
    }

    @Override
    public boolean matches(Object o) {
        return filterClass.isAssignableFrom(o.getClass());
    }
}
