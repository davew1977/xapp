package xapp.mdda.model;


import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.PreInit;
import net.sf.xapp.annotations.objectmodelling.ValidImplementations;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/8/14
 * Time: 7:24 AM
 * To change this template use File | Settings | File Templates.
 */
@ValidImplementations({DirMeta.class, Type.class, Api.class})
public class FileMeta implements Cloneable {
    private String name = "";

    public FileMeta(String name) {
        this.name = name;
    }

    public FileMeta() {
    }

    @Key
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public FileMeta clone() throws CloneNotSupportedException {
        FileMeta clone = (FileMeta) super.clone();
        clone.setName(clone.getName() + " (copy)");
        return clone;
    }

}