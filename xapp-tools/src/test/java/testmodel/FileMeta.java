package testmodel;

import net.sf.xapp.annotations.objectmodelling.Key;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileMeta {
    private String name;

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
