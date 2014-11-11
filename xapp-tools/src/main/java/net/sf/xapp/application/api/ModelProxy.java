package net.sf.xapp.application.api;

/**
 * If we want the underlying model AND the framework to be properly updated, the app dev must perform updates through this api
 */
public interface ModelProxy {


    /**
     * save an object in the model at the given location (if location is a list, then it will be added to the end
     */
    <T> void add(Object parent, String property, T obj);

    /**
     * creates an empty object and checks it out
     */
    <T> T create(Object parent, String property, Class<T> type);

    /**
     * creates an empty object and checks it out
     */
    <T> T create(Long parentId, String property, Class<T> type);

    /**
     * signal from the app that it wishes to modify the given object
     */
    <T> T checkout(T obj);

    /**
     * signal from the app that it wishes to modify the given object
     */
    <T> T checkout(Class<T> type, Long id);

    /**
     * commit the changes to the model (and the server, if we are distributed)
     */
    <T> void commit(T obj);

    /**
     * end a checkout without a commit, note that this will not revert any changes made locally to the object
     */
    <T> void cancelCheckout(T obj);

    /**
     * delete the obj
     */
    <T> void delete(T obj);
}
