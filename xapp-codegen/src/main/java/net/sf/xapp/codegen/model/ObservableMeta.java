package net.sf.xapp.codegen.model;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 4/14/14
 * Time: 8:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObservableMeta {
    /**
     * setting this to true means that a principal field will be added to the update api in order to track who
     * made the update
     */
    private boolean usersCanUpdate;

    public boolean isUsersCanUpdate() {  //todo this is currently ignored
        return usersCanUpdate;
    }

    public void setUsersCanUpdate(boolean usersCanUpdate) {
        this.usersCanUpdate = usersCanUpdate;
    }
}
