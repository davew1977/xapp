package net.sf.xapp.objectmodelling.core.filters;

import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.utils.Filter;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public enum PropertyFilter implements Filter<Property> {
    COMPLEX_NON_REFERENCE {
        @Override
        public boolean matches(Property property) {
            return property.isComplexNonReference();
        }
    }, REFERENCE {
        @Override
        public boolean matches(Property property) {
            return property.isReference() || (property instanceof ContainerProperty && ((ContainerProperty) property).containsReferences());
        }
    },LIST {
        @Override
        public boolean matches(Property property) {
            return property.isList();
        }
    }, ALL {
        @Override
        public boolean matches(Property property) {
            return true;
        }
    }, CONVERTIBLE_TO_STRING {
        @Override
        public boolean matches(Property property) {
            return !property.isContainer() && property.canConvertToString();
        }
    }

}
