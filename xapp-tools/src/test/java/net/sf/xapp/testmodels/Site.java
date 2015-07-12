package net.sf.xapp.testmodels;

import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;

import java.util.List;

/**
 * Created by oldDave on 07/07/2015.
 */
@NamespaceFor(Category.class)
public class Site {
    @Key
    private String name;
    private List<Category> categories;

    @Override
    public String toString() {

        return name;
    }
}
