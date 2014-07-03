package net.sf.xapp.objectmodelling.core.filters;

import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.utils.Filter;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public enum PropertyFilter implements Filter<Property> {
    COMPLEX_NON_REFERENCE {
        @Override
        public boolean matches(Property property) {
            return property.isComplexNonReference();
        }
    };

}
