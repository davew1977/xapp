package net.sf.xapp.application.api;

/**
 * If we want the underlying model AND the framework to be properly updated, the app dev must perform updates through this api
 */
public interface ModelProxy {


    /**
     * save an object in the model at the given location (if location is a list, then it will be added to the end
     */
    <T> T add(Object parent, String property, T obj);
    <T> T add(Object parent, T obj);

    /**
     * creates an empty object and checks it out
     */
    <T> T create(Object parent, String property, Class<T> type);
    <T> T create(Object parent, Class<T> type);

    /**
     * creates an empty object and checks it out
     */
    <T> T create(Long parentId, String property, Class<T> type);
    <T> T create(Long parentId, Class<T> type);
    /**
     * signal from the app that it wishes to modify the given object
     */
    <T> T checkout(T obj);

    /**
     * signal from the app that it wishes to modify the given object
     */
    <T> T checkout(Long id);

    /**
     * commit the changes to the model (and the server, if we are distributed)
     */
    void commit(Object... objects);

    void moveInList(Object parent, String property, Object objectToMove, int delta);
    void moveInList(Object parent, Object objectToMove, int delta);

    /**
     * end a checkout without a commit, note that this will not revert any changes made locally to the object
     */
    <T> void cancelCheckout(T obj);

    /**
     * delete the obj
     */
    <T> void delete(T obj);

    void addRefs(Object parent, String property, Object... objectsToAdd);
    void addRefs(Object parent, Object... objectsToAdd);
    void removeRefs(Object parent, String property, Object... objectsToRemove);
    void removeRefs(Object parent, Object... objectsToRemove);
    void changeType(Object obj, Class newType);

    <T> T getModel();

    void moveTo(Object parent, String property, Object objectToMove);
    void moveTo(Object parent, Object objectToMove);
}
