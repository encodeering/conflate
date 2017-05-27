package com.encodeering.conflate.experimental.api

/**
 * A dispatcher is connected to exactly one [storage][Storage] and starts a mutation cycle for every [passing][dispatch] action intent.
 *
 * The process of a cycle is completely covered by [Coroutines][https://kotlinlang.org/docs/reference/coroutines.html] and can be described as follows:
 *
 * - Enter dispatch
 * - Enter middleware - one by one in the configured order
 * - Perform conflation
 * - Perform notification
 * - Exit middleware - one by one in reversed order
 * - Exit dispatch
 *
 * A middleware is encouraged to linearize their routines with this process, but may define rare
 * exceptions for processes, that run independently in their own context, e.g. saga, epic, ..
 *
 * Please consult the middleware specific documentation here.
 *
 * ### Note
 *
 * Conflate provides a dedicated entity, whereas Redux has left this responsibility to the storage, but this is more likely a design choice.
 *
 * ### Official documentation:
 *
 * - Dispatches an action. This is the only way to trigger a state change. [http://redux.js.org/docs/api/Store.html#dispatchaction]
 *
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Dispatcher {

    /**
     * Starts a mutation cycle and returns an observable (future-like) to watch this process.
     *
     * The observable completes with the same result, exactly when the mentioned process completes.
     *
     * @param action specifies the action intent
     * @return an observable for this process
     */
    fun dispatch (action : Action) : Completable<Dispatcher, Unit>

}