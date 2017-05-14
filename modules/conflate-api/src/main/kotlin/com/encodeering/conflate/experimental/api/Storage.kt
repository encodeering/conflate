package com.encodeering.conflate.experimental.api

/**
 * A storage acts as only provider to [access][state] and [mutate][dispatcher] the predefined state container.
 *
 * An uni-directional workflow can be described as follows:
 *
 * - A user-interaction or service-schedule will dispatch an action intent
 * - A dispatched intent can be changed or serviced by zero or more participating middleware components
 * - Any passing intent will be applied to the state container by the reducer eventually
 * - All listeners will be notified about an occurring change
 *
 * The invocation order for listeners is unspecified and may not stay in the perceived order.
 *
 * ### Concurrency
 *
 * Asynchronicity will be handled by [middleware][Middleware] components, which means that the order, in which intents get applied by the reducer, is unspecified.
 * However, environments and platforms might already define strategies and rules with respect to:
 *
 * - Memory visibility
 * - Threading behaviour
 * - Stack execution
 *
 * An implementation must adhere to the rules of the target platform.
 *
 * Please consult the platform specific documentation here.
 *
 * ### Official documentation:
 *
 * - The Store is the object that brings them together. The store has the following responsibilities: [http://redux.js.org/docs/basics/Store.html]
 *    - Holds application state
 *    - Allows access to state
 *    - Allows state to be updated
 *    - Registers listeners via subscribe
 *
 * @param State defines the type of the state container
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Storage<out State> {

    /**
     * Provides access to the last known state container, which differs for subsequent calls.
     */
    val state : State

    /**
     * Provides access to the connected action dispatcher.
     */
    val dispatcher : Dispatcher

    /**
     * Attaches a listener that will be notified on every state change.
     *
     * Any thrown exception will be silently ignored to preserve callback isolation.
     *
     * @param listener specifies a callback function
     * @return a handle to un-subscribe [this][listener] callback function
     */
    fun subscribe (listener : () -> Unit) : Runnable

}

/**
 * Creates a view on the state container.
 *
 * Usually used by components that read values from the state container.
 *
 * @param r specifies a view function
 * @param State specifies the context reference to the last known state container
 */
fun <State>       Storage<State>.unbox (r : State.(Dispatcher) -> Unit) = state.r (dispatcher)

/**
 * Creates a view on a subset of the state container.
 *
 * Usually used by components that read values from the state container.
 *
 * @param t specifies a transform function to retrieve the subset
 * @param r specifies a view function
 * @param Part specifies the context reference to the last known subset container
 * @param State specifies the context reference to the last known state container
 */
fun <State, Part> Storage<State>.unbox (t : State.() -> Part, r : Part.(Dispatcher) -> Unit) = state.t ().r (dispatcher)