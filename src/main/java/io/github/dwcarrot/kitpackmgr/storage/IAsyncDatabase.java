package io.github.dwcarrot.kitpackmgr.storage;

import java.nio.channels.CompletionHandler;

public interface IAsyncDatabase<C, D> {

    /**
     * create / insert data into DB
     * @param condition create condition
     * @param data data to add
     * @param callback callback function after created. call with data of created.
     * @param attachment callback function attachment
     * @param linked if this method is called inside a callback of IAsyncDatabase, set linked to {@code true} means
     *               this operation will be executed exactly after the callback. In this case, MO MORE THAN ONE operation
     *               can exist in each callback.
     *               {@code false} means this operation may wait in a queue.
     *               if this method is called independently, linked should never be set to {@code true}, or undefined
     *               behavior will happen.
     * @param <A>
     */
    <A> void create(C condition, D data, CompletionHandler<D, A> callback, A attachment, boolean linked);

    /**
     * retrieve / query data from DB
     * @param condition query condition
     * @param callback callback function after retrieved data. call with data.
     * @param attachment
     * @param linked @see IAsyncDatabase.create
     * @param <A>
     */
    <A> void retrieve(C condition, CompletionHandler<D, A> callback, A attachment, boolean linked);

    /**
     * update data in DB
     * @param condition update condition
     * @param data callback function after retrieved data. call with new data.
     * @param callback
     * @param attachment
     * @param linked @see IAsyncDatabase.create
     * @param <A>
     */
    <A> void update(C condition, D data, CompletionHandler<D, A> callback, A attachment, boolean linked);

    /**
     * delete data in DB
     * @param condition delete condition
     * @param callback  callback function after retrieved data. call with deleted data.
     * @param attachment
     * @param linked @see IAsyncDatabase.create
     * @param <A>
     */
    <A> void delete(C condition, CompletionHandler<D, A> callback, A attachment, boolean linked);

    /**
     * try to retrieve / query data from DB
     * @param condition query condition
     * @return data itself, or {@code null} if data can not be loaded immediately
     */
    D tryRetrieve(C condition);
}
