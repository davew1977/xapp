package net.sf.xapp.testmodels;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.NamespaceFor;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;
import net.sf.xapp.objectmodelling.core.AbstractNode;

import java.util.List;

/**
 */
@NamespaceFor(Category.class)
@Container(listProperty = "subCategories")
@ValidImplementations({SpecialCategory.class})
public class Category extends AbstractNode<Category> {

    private List<Category> subCategories;

    public Category() {
        super(Category.class);
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories;
    }
}
