package com.jk.changehandler.change.model;

/**
 * interface to be extended by all the db change events.
 *
 * @param <T>
 */
public interface ChangeEvent<T> {
    /**
     * get Old db item
     * @return
     */
    public T getOldItem();

    /**
     * get New Db item
     * @return
     */
    public T getNewItem();

    /**
     * get type of change, i.e, whether this was an insert/remove/modify
     * @return
     */
    public ChangeEventType getEventType();
}

