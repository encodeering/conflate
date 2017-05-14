package com.encodeering.conflate.experimental.api

/**
 * A middleware defines an extension mechanism to integrate business services or to provide platform specific services
 * and might therefore provide custom actions or other building blocks to specify the behaviour of the middleware component.
 *
 * As mentioned by the process of a [dispatch][Dispatcher] Cycle, a middleware component should state, whether and under what conditions
 *
 * - Linearization
 * - Thread-Safety
 *
 * is supported.
 *
 * A platform or technology specific middleware may freely choose to adhere to the rules of the target platform only, as stated by the [Storage],
 * but any other middleware is encouraged to implement a thread-safe behaviour, if they target multiple platforms.
 *
 * Please consult the platform specific middleware documentation here.
 *
 * ### Linearization
 *
 * A middleware is considered to support linearization, if the incoming action and all internally derived actions have been processed by
 *
 * - this middleware component
 * - subsequent middleware components - including [Connection.next] and [Connection.initial]
 *
 * and return can be safely given back to the caller.
 *
 * ### Sample
 *
 * ```
 * class Http<in State> : Middleware<State> {
 *
 *     data class Get (val url : String, val content : String = "") : Action
 *
 *     override fun interceptor (connection : Middleware.Connection<State>) : Middleware.Interceptor {
 *         return object : Middleware.Interceptor {
 *
 *             suspend override fun dispatch     (action : Action) {
 *                 return when                   (action) {
 *                     is Get -> connection.next (action.copy (content = get (action.url)))
 *                     else   -> connection.next (action)
 *                 }
 *             }
 *
 *             suspend fun get (url : String) : String {
 *                 // suspendable async computation
 *             }
 *
 *         }
 *     }
 *
 * }
 * ```
 *
 * ### Official documentation:
 *
 * - It provides a third-party extension point between dispatching an action, and the moment it reaches the reducer.
 * People use Redux middleware for logging, crash reporting, talking to an asynchronous API, routing, and more. [http://redux.js.org/docs/advanced/Middleware.html]
 *
 * @param State defines the type of the state container
 * @author Michael Clausen - encodeering@gmail.com
 */
interface Middleware<in State> {

    /**
     * Derives an interceptor from this middleware component.
     *
     * @param connection specifies a reference to the environment
     * @return an interceptor
     */
    fun interceptor (connection : Connection<State>) : Interceptor

    /**
     * A connection arranges the communication between a middleware component and the outer environment.
     */
    interface Connection<out State> {

        /**
         * Provides access to the last known state container, which differs for subsequent calls.
         */
        val state : State

        /**
         * Dispatches the action to the initial middleware component
         *
         * @param action specifies the action intent
         */
        suspend fun initial (action : Action)

        /**
         * Dispatches the action to the next middleware component
         *
         * @param action specifies the action intent
         */
        suspend fun next    (action : Action)

    }

    /**
     * An interceptor defines the extension logic of a middleware component.
     */
    interface Interceptor {

        /**
         * Intercepts the incoming action.
         *
         * Any intent, that is not passed by the underlying [connection][Connection], will be silently ignored.
         *
         * @param action specifies the action intent
         */
        suspend fun dispatch (action : Action)

    }

}